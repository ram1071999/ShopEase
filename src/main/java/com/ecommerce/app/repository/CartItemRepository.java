package com.ecommerce.app.repository;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProduct(User user, Product product);
    void deleteByUser(User user);
}
