package com.ecommerce.app.controller;

import com.ecommerce.app.dto.OrderStatusUpdateRequest;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/products")
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product saved = productRepository.save(product);
        return ResponseEntity.ok(saved);
    }

    // ---------- Soft-delete a product (avoids FK violation with order_items) ----------
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setActive(false);
        productRepository.save(product);

        Map<String, Object> response = new HashMap<>();
        response.put("id", product.getId());
        response.put("active", product.getActive());
        response.put("message", "Product deactivated (soft-deleted) successfully");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id,
                                                     @Valid @RequestBody OrderStatusUpdateRequest request) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        List<Order> orders = orderService.getAllOrders();
        List<Product> products = productRepository.findAll();

        BigDecimal totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != Order.OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingOrders = orders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.PENDING)
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orders.size());
        stats.put("pendingOrders", pendingOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalProducts", products.size());
        stats.put("totalUsers", userRepository.count());

        return stats;
    }
}
