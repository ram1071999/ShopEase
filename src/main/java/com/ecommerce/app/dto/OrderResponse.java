package com.ecommerce.app.dto;

import com.ecommerce.app.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Customer-facing order view — nests ProductPublicResponse per item so supplier
 * name, supplier URL, cost price, and SKU never reach the storefront.
 */
@Data
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private List<OrderItemResponse> items;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private Order.OrderStatus status;
    private LocalDateTime createdAt;

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
