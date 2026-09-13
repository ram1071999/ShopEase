package com.ecommerce.app.controller;

import com.ecommerce.app.dto.CartItemRequest;
import com.ecommerce.app.dto.CartItemResponse;
import com.ecommerce.app.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing cart endpoints. Responses use CartItemResponse (nesting
 * ProductPublicResponse) so supplier/cost data never reaches the storefront.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public List<CartItemResponse> getCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return cartService.getCart(userId).stream().map(CartItemResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(HttpServletRequest request, @Valid @RequestBody CartItemRequest body) {
        Long userId = (Long) request.getAttribute("currentUserId");
        var item = cartService.addToCart(userId, body.getProductId(), body.getQuantity());
        return ResponseEntity.ok(CartItemResponse.from(item));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponse> updateQuantity(HttpServletRequest request,
                                                             @PathVariable Long cartItemId,
                                                             @RequestParam Integer quantity) {
        Long userId = (Long) request.getAttribute("currentUserId");
        var item = cartService.updateQuantity(userId, cartItemId, quantity);
        return ResponseEntity.ok(CartItemResponse.from(item));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeItem(HttpServletRequest request, @PathVariable Long cartItemId) {
        Long userId = (Long) request.getAttribute("currentUserId");
        cartService.removeFromCart(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
