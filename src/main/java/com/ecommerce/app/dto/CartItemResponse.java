package com.ecommerce.app.dto;

import com.ecommerce.app.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartItemResponse {
    private Long id;
    private ProductPublicResponse product;
    private Integer quantity;

    public static CartItemResponse from(CartItem item) {
        return new CartItemResponse(item.getId(), ProductPublicResponse.from(item.getProduct()), item.getQuantity());
    }
}
