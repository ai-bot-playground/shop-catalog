package com.shop.catalog.api;

import com.shop.catalog.domain.Product;
import com.shop.catalog.repo.ProductRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Test-support endpoints used by acceptance tests to provision/clean up data.
 * Enabled only in non-prod environments via {@code shop.test-support.enabled=true}.
 */
@RestController
@RequestMapping("/products")
@ConditionalOnProperty(name = "shop.test-support.enabled", havingValue = "true")
public class ProductTestSupportController {

    private final ProductRepository repo;

    public ProductTestSupportController(ProductRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody CreateProductRequest req) {
        Product saved = repo.save(new Product(req.name(), req.description(), req.price()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", saved.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
