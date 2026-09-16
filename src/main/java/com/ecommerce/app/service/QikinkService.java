package com.ecommerce.app.service;

import com.ecommerce.app.config.QikinkProperties;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.OrderItem;
import com.ecommerce.app.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pushes orders to Qikink's Print-on-Demand / Dropshipping API so their
 * fulfilled line items get printed, packed and shipped automatically.
 *
 * Only OrderItems whose Product has supplierName == "Qikink" (case-insensitive)
 * AND a non-blank supplierSku are included — everything else in ShopEase is
 * unaffected, so you can mix Qikink products with manually-fulfilled ones in
 * the same order.
 *
 * This is best-effort: any failure (missing credentials, network error, a
 * product missing its SKU) is logged and swallowed rather than thrown, so a
 * Qikink outage never blocks updating an order's status in ShopEase itself.
 * Check the application logs (or Order.qikinkPushed) to confirm a push worked.
 *
 * NOTE: Qikink's request field names (esp. the shipping-address state/province
 * key) were reconstructed from public integration write-ups, not a live
 * Postman collection. If Qikink's dashboard shows a validation error, log the
 * raw response body (see the catch block below) and adjust buildOrderPayload
 * accordingly — the structure here is a solid starting point, not guaranteed
 * byte-for-byte correct against the current live API.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class QikinkService {

    private final QikinkProperties properties;
    private final RestTemplate restTemplate;

    private String baseUrl() {
        return properties.isSandbox() ? "https://sandbox.qikink.com" : "https://api.qikink.com";
    }

    /** Pushes only this order's Qikink-fulfilled items. Never throws. */
    public void pushOrderIfApplicable(Order order) {
        if (!properties.isEnabled()) {
            log.debug("Qikink integration disabled (qikink.enabled=false) — skipping push for order {}", order.getId());
            return;
        }
        if (order.isQikinkPushed()) {
            log.debug("Order {} already pushed to Qikink — skipping", order.getId());
            return;
        }

        List<OrderItem> qikinkItems = order.getItems().stream()
                .filter(this::isQikinkFulfilled)
                .toList();

        if (qikinkItems.isEmpty()) {
            log.debug("Order {} has no Qikink-fulfilled items — nothing to push", order.getId());
            return;
        }

        try {
            String token = fetchAccessToken();
            Map<String, Object> payload = buildOrderPayload(order, qikinkItems);
            sendCreateOrder(token, payload);
            order.setQikinkPushed(true);
            log.info("Pushed order {} ({} item(s)) to Qikink", order.getId(), qikinkItems.size());
        } catch (Exception e) {
            // Never let a Qikink failure block the admin's status update.
            log.error("Failed to push order {} to Qikink: {}", order.getId(), e.getMessage(), e);
        }
    }

    private boolean isQikinkFulfilled(OrderItem item) {
        var product = item.getProduct();
        return product != null
                && product.getSupplierName() != null
                && product.getSupplierName().trim().equalsIgnoreCase("qikink")
                && product.getSupplierSku() != null
                && !product.getSupplierSku().isBlank();
    }

    private String fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("ClientId", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
                baseUrl() + "/api/token", request, Map.class);

        if (response == null || response.get("Accesstoken") == null) {
            throw new RestClientException("Qikink token response missing Accesstoken: " + response);
        }
        return response.get("Accesstoken").toString();
    }

    private Map<String, Object> buildOrderPayload(Order order, List<OrderItem> items) {
        User user = order.getUser();
        String[] nameParts = splitName(user.getName());

        BigDecimal totalValue = items.stream()
                .map(i -> i.getProduct().getCostPrice() != null
                        ? i.getProduct().getCostPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                        : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> lineItems = new ArrayList<>();
        for (OrderItem item : items) {
            Map<String, Object> line = new HashMap<>();
            line.put("search_from_my_products", 1);
            line.put("sku", item.getProduct().getSupplierSku());
            line.put("quantity", String.valueOf(item.getQuantity()));
            line.put("price", item.getProduct().getCostPrice() != null
                    ? item.getProduct().getCostPrice().toPlainString() : "0");
            lineItems.add(line);
        }

        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("first_name", nameParts[0]);
        shippingAddress.put("last_name", nameParts[1]);
        shippingAddress.put("address1", order.getShippingAddress());
        shippingAddress.put("address2", "");
        shippingAddress.put("phone", order.getShippingPhone() != null ? order.getShippingPhone() : user.getPhone());
        shippingAddress.put("email", user.getEmail());
        shippingAddress.put("city", order.getShippingCity());
        shippingAddress.put("zip", order.getShippingZip());
        shippingAddress.put("state", order.getShippingState());
        shippingAddress.put("country", "India");

        Map<String, Object> payload = new HashMap<>();
        // Qikink caps order_number at 15 chars.
        payload.put("order_number", ("SE" + order.getId()).substring(0, Math.min(15, ("SE" + order.getId()).length())));
        payload.put("qikink_shipping", "1");
        payload.put("gateway", "Prepaid");
        payload.put("total_order_value", totalValue.toPlainString());
        payload.put("line_items", lineItems);
        payload.put("shipping_address", shippingAddress);
        return payload;
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[] {"Customer", ""};
        String[] parts = fullName.trim().split("\\s+", 2);
        return parts.length == 2 ? parts : new String[] {parts[0], ""};
    }

    private void sendCreateOrder(String token, Map<String, Object> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Clientid", properties.getClientId());
        headers.set("Accesstoken", token);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        restTemplate.postForObject(baseUrl() + "/api/order/create", request, String.class);
    }
}
