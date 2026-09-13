package com.ecommerce.app.dto;

import com.ecommerce.app.model.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderStatusUpdateRequest {
    @NotNull
    private Order.OrderStatus status;
}
