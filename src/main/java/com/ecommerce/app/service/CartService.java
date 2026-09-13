package com.ecommerce.app.service;

import com.ecommerce.app.model.CartItem;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.User;
import com.ecommerce.app.repository.CartItemRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<CartItem> getCart(Long userId) {
        User user = getUser(userId);
        return cartItemRepository.findByUser(user);
    }

    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        User user = getUser(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        CartItem item = cartItemRepository.findByUserAndProduct(user, product)
                .orElse(new CartItem(null, user, product, 0));

        item.setQuantity(item.getQuantity() + quantity);
        return cartItemRepository.save(item);
    }

    public CartItem updateQuantity(Long userId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to modify this cart item");
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void removeFromCart(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to remove this cart item");
        }

        cartItemRepository.delete(item);
    }

    public void clearCart(Long userId) {
        User user = getUser(userId);
        cartItemRepository.deleteByUser(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
