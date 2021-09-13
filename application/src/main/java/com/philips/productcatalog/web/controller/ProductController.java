package com.philips.productcatalog.web.controller;

import com.philips.productcatalog.core.domain.Product;
import com.philips.productcatalog.core.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
public class ProductController {
    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("")
    List<Product> listAll() {
        return productService.listAll();
    }

    @PostMapping("")
    ResponseEntity<Product> createProduct(@RequestBody Product product) {
        if (product.getId() != null && productService.findById(product.getId()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok(productService.create(product));
    }

    @GetMapping("/{id}")
    ResponseEntity<Product> findById(@PathVariable UUID id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    ResponseEntity<Product> updateProduct(@RequestBody Product product, @PathVariable UUID id) {
        if (product.getId() != null && !product.getId().equals(id)) {
            return ResponseEntity.badRequest().build();
        }

        return productService.findById(id)
                .map(oldProduct -> productService.update(product))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Object> deleteProduct(@PathVariable UUID id) {
        return productService.findById(id)
                .map(oldProduct -> {
                    productService.delete(oldProduct);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
