package com.shop.catalog.api;

import com.shop.catalog.repo.ProductRepository;
import com.shop.catalog.service.UserSessionService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;
    private final UserSessionService userSessionService;

    public ProductController(ProductRepository repo, UserSessionService userSessionService) {
        this.repo = repo;
        this.userSessionService = userSessionService;
    }

    @GetMapping
    public PagedModel<ProductResponse> list(Pageable pageable,
                                            @CookieValue(value = "SESSION", required = false) String sessionToken) {
        BigDecimal margin = userSessionService.resolveMargin(sessionToken);
        return new PagedModel<>(repo.findAll(pageable).map(p -> ProductResponse.from(p, margin)));
    }

    @GetMapping("/search")
    public PagedModel<ProductResponse> search(@RequestParam("q") String q, Pageable pageable,
                                              @CookieValue(value = "SESSION", required = false) String sessionToken) {
        BigDecimal margin = userSessionService.resolveMargin(sessionToken);
        return new PagedModel<>(repo.findByNameContainingIgnoreCase(q, pageable).map(p -> ProductResponse.from(p, margin)));
    }

    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id + ':' + (#sessionToken != null ? #sessionToken : 'anon')")
    public ProductResponse get(@PathVariable Long id,
                               @CookieValue(value = "SESSION", required = false) String sessionToken) {
        BigDecimal margin = userSessionService.resolveMargin(sessionToken);
        return repo.findById(id)
                .map(p -> ProductResponse.from(p, margin))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
