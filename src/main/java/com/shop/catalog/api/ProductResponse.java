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

    public static ProductResponse from(Product p, BigDecimal marginPercent) {
        BigDecimal basePrice = p.getPrice();
        BigDecimal finalPrice = basePrice;
        if (marginPercent != null && marginPercent.compareTo(BigDecimal.ZERO) != 0) {
            finalPrice = basePrice.multiply(BigDecimal.ONE.add(marginPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getDescription(),
                finalPrice,
                p.getImageUrl(),
                p.getCategory() != null ? p.getCategory().getName() : null);
    }
}
