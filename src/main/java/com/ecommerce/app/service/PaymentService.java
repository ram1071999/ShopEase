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
     * Razorpay order banata hai.
     * - Sirf order ka owner payment shuru kar sakta hai.
     * - Amount backend ke totalAmount se aata hai (OrderService DB ki price se nikalta hai), app se nahi.
     */
    @Transactional
    public Map<String, Object> createRazorpayOrder(Long userId, Long orderId) {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new RuntimeException("Online payment abhi available nahi hai");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }
        if ("PAID".equals(order.getPaymentStatus())) {
            throw new RuntimeException("Order already paid");
        }
        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cancelled hai");
        }

        BigDecimal total = order.getTotalAmount();
        if (total == null) {
            total = order.getItems().stream()
                    .map(i -> i.getPriceAtPurchase().multiply(BigDecimal.valueOf(i.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

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
     * Razorpay ka signature verify karta hai. Sahi ho aur order isi user ka ho, tabhi PAID hota hai.
     * Qikink push yahan nahi hota. PaymentController isko payment commit hone ke BAAD
     * OrderService.confirmAfterPayment se karata hai, taaki Qikink ka error payment ko na roke.
     *
     * @return paid order ka id, ya null agar verification fail hui
     */
    @Transactional
    public Long verifyAndMarkPaid(Long userId, String rzpOrderId, String rzpPaymentId, String signature) {
        if (rzpOrderId == null || rzpPaymentId == null || signature == null) {
            return null;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String expected = HexFormat.of().formatHex(
                    mac.doFinal((rzpOrderId + "|" + rzpPaymentId).getBytes(StandardCharsets.UTF_8)));

            if (!MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

        Order order = orderRepository.findByRazorpayOrderId(rzpOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }

        if (!"PAID".equals(order.getPaymentStatus())) {
            order.setPaymentStatus("PAID");
            order.setRazorpayPaymentId(rzpPaymentId);
            orderRepository.save(order);
        }
        return order.getId();
    }
}
