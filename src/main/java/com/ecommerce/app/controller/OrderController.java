package com.ecommerce.app.controller;

import com.ecommerce.app.dto.OrderRequest;
import com.ecommerce.app.dto.OrderResponse;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing order endpoints. Responses use OrderResponse (nesting
 * ProductPublicResponse) so supplier/cost data never reaches the storefront.
 * Admins see full supplier info via AdminController's /api/admin/orders.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(HttpServletRequest request, @Valid @RequestBody OrderRequest body) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Order order = orderService.placeOrder(userId, body);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping
    public List<OrderResponse> getMyOrders(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return orderService.getUserOrders(userId).stream().map(OrderResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}
