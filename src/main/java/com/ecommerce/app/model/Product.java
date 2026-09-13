package com.ecommerce.app.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private String imageUrl;

    private String category;

    private Integer stockQuantity;

    private Double rating;

    // ---------- Dropshipping supplier link fields ----------

    /** Name of the supplier/platform this product is sourced from, e.g. "AliExpress", "CJ Dropshipping", "Meesho" */
    private String supplierName;

    /** Direct URL to the product on the supplier's site, used to place the actual order when a sale comes in */
    @Column(length = 1000)
    private String supplierUrl;

    /** What the supplier charges per unit (your cost) — used to compute profit margin against the selling price */
    private BigDecimal costPrice;

    /** Supplier's own SKU / product ID, useful when reordering or matching on their platform */
    private String supplierSku;
}
