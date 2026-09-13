package com.ecommerce.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingAddress;
}
