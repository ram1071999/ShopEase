package com.ecommerce.app.controller;

import com.ecommerce.app.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> create(HttpServletRequest request, @PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return ResponseEntity.ok(paymentService.createRazorpayOrder(userId, orderId));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request, @RequestBody Map<String, String> body) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Long paidOrderId = paymentService.verifyAndMarkPaid(
                userId,
                body.get("razorpayOrderId"),
                body.get("razorpayPaymentId"),
                body.get("razorpaySignature"));

        return paidOrderId != null
                ? ResponseEntity.ok(Map.of("status", "PAID"))
                : ResponseEntity.badRequest().body(Map.of("status", "FAILED"));
    }
}
