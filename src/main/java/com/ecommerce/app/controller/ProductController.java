package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductPublicResponse;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Public storefront endpoints. Responses use ProductPublicResponse so supplier
 * name, supplier URL, cost price, and supplier SKU never reach customers.
 * Admins manage the full Product (with supplier fields) via AdminController.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductPublicResponse> getAll(@RequestParam(required = false) String category,
                                               @RequestParam(required = false) String search) {
        List<Product> products;
        if (search != null && !search.isBlank()) {
            products = productService.search(search);
        } else if (category != null && !category.isBlank()) {
            products = productService.getByCategory(category);
        } else {
            products = productService.getAllProducts();
        }
        return products.stream().map(ProductPublicResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductPublicResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ProductPublicResponse.from(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
