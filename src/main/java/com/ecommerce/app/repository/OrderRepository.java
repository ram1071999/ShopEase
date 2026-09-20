package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findAllByOrderByCreatedAtDesc();
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
}
