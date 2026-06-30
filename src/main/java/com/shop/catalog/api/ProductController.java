package com.shop.catalog.api;

import com.shop.catalog.repo.ProductRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;
    private final UserMarginService marginService;

    public ProductController(ProductRepository repo, UserMarginService marginService) {
        this.repo = repo;
        this.marginService = marginService;
    }

    @GetMapping
    public PagedModel<ProductResponse> list(
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        BigDecimal margin = marginService.resolveMargin(userId);
        return new PagedModel<>(repo.findAll(pageable)
                .map(ProductResponse::from)
                .map(r -> applyMargin(r, margin)));
    }

    @GetMapping("/search")
    public PagedModel<ProductResponse> search(
            @RequestParam("q") String q,
            Pageable pageable,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        BigDecimal margin = marginService.resolveMargin(userId);
        return new PagedModel<>(repo.findByNameContainingIgnoreCase(q, pageable)
                .map(ProductResponse::from)
                .map(r -> applyMargin(r, margin)));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id + '-' + (#userId ?: 'anonymous')")
    public ProductResponse get(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        BigDecimal margin = marginService.resolveMargin(userId);
        return repo.findById(id)
                .map(ProductResponse::from)
                .map(r -> applyMargin(r, margin))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private ProductResponse applyMargin(ProductResponse response, BigDecimal margin) {
        if (margin == null || margin.compareTo(BigDecimal.ZERO) == 0) {
            return response;
        }
        BigDecimal adjustedPrice = response.getPrice()
                .multiply(BigDecimal.ONE.add(margin))
                .setScale(2, RoundingMode.HALF_UP);
        response.setPrice(adjustedPrice);
        return response;
    }
}

@Service
class UserMarginService {

    private static final BigDecimal DEFAULT_MARGIN = BigDecimal.ZERO;

    private static final Map<Long, BigDecimal> USER_MARGINS = Map.of(
            1L, new BigDecimal("0.10"),
            2L, new BigDecimal("0.20"),
            3L, new BigDecimal("0.15")
    );

    public BigDecimal resolveMargin(Long userId) {
        if (userId == null) {
            return DEFAULT_MARGIN;
        }
        return USER_MARGINS.getOrDefault(userId, DEFAULT_MARGIN);
    }
}
