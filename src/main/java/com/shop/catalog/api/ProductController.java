package com.shop.catalog.api;

import com.shop.catalog.repo.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public PagedModel<ProductResponse> list(Pageable pageable) {
        return new PagedModel<>(repo.findAll(pageable).map(ProductResponse::from));
    }

    @GetMapping("/search")
    public PagedModel<ProductResponse> search(@RequestParam("q") String q, Pageable pageable) {
        return new PagedModel<>(repo.findByNameContainingIgnoreCase(q, pageable).map(ProductResponse::from));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id")
    public ProductResponse get(@PathVariable Long id) {
        return repo.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
