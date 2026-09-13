package com.ecommerce.app.dto;

import com.ecommerce.app.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private ProductPublicResponse product;
    private Integer quantity;
    private BigDecimal priceAtPurchase;

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                ProductPublicResponse.from(item.getProduct()),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }
}
