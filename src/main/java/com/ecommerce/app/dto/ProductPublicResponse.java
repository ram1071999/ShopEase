package com.ecommerce.app.dto;

import com.ecommerce.app.model.Product;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Customer-facing product view. Deliberately excludes supplier name, supplier URL,
 * cost price, and supplier SKU — that's internal sourcing/margin data and must never
 * reach the public storefront API.
 */
@Data
@AllArgsConstructor
public class ProductPublicResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private Integer stockQuantity;
    private Double rating;

    public static ProductPublicResponse from(Product p) {
        return new ProductPublicResponse(
                p.getId(), p.getName(), p.getDescription(), p.getPrice(),
                p.getImageUrl(), p.getCategory(), p.getStockQuantity(), p.getRating()
        );
    }
}
