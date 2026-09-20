package com.ecommerce.app.service;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final RestTemplate rest = new RestTemplate();

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    /**
     * Razorpay order banata hai. Amount hamesha DB ki price se nikalta hai, app se nahi.
     */
    @Transactional
    public Map<String, Object> createRazorpayOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if ("PAID".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("Order already paid");
        }

        BigDecimal total = order.getItems().stream()
                .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paise = total.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(keyId, keySecret);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "amount", paise,
                "currency", "INR",
                "receipt", "order_" + orderId);

        ResponseEntity<Map> res = rest.postForEntity(
                "https://api.razorpay.com/v1/orders",
                new HttpEntity<>(body, headers),
                Map.class);

        String rzpOrderId = (String) res.getBody().get("id");

        order.setPaymentMethod("ONLINE");
        order.setPaymentStatus("PENDING");
        order.setRazorpayOrderId(rzpOrderId);
        orderRepository.save(order);

        return Map.of(
                "razorpayOrderId", rzpOrderId,
                "amount", paise,
                "currency", "INR",
                "keyId", keyId);
    }

    /**
     * Razorpay ka signature verify karta hai. Sahi ho tabhi order PAID hota hai.
     */
    @Transactional
    public boolean verifyAndMarkPaid(String rzpOrderId, String rzpPaymentId, String signature) {
        if (rzpOrderId == null || rzpPaymentId == null || signature == null) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(
                    mac.doFinal((rzpOrderId + "|" + rzpPaymentId).getBytes(StandardCharsets.UTF_8)));

            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8))) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }

        Order order = orderRepository.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setPaymentStatus("PAID");
        order.setRazorpayPaymentId(rzpPaymentId);
        orderRepository.save(order);

        // TODO: yahan apna existing Qikink push call lagao (payment ke baad hi)

        return true;
    }
}
