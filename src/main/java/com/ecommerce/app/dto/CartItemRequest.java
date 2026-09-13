package com.ecommerce.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull
    private Long productId;

    @Min(1)
    private Integer quantity;
}
