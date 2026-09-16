package com.ecommerce.app.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingAddress;

    // Optional, but required for orders containing Qikink-fulfilled items to be
    // auto-pushed to Qikink (their API needs city/state/zip/phone separately).
    private String shippingCity;
    private String shippingState;
    private String shippingZip;
    private String shippingPhone;
}
