package com.shop.catalog.api;

import com.shop.catalog.domain.Product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        String category) {

    public static ProductResponse from(Product p) {
        return from(p, BigDecimal.ZERO);
    }

    public static ProductResponse from(Product p, BigDecimal margin) {
        BigDecimal adjusted = applyMargin(p.getPrice(), margin);
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                adjusted,
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getName() : null);
    }

    static BigDecimal applyMargin(BigDecimal basePrice, BigDecimal margin) {
        if (basePrice == null) {
            return null;
        }
        BigDecimal effectiveMargin = margin == null ? BigDecimal.ZERO : margin;
        BigDecimal multiplier = BigDecimal.ONE.add(effectiveMargin);
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
}
