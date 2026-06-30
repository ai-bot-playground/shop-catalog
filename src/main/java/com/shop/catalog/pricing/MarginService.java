package com.shop.catalog.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MarginService {

    private static final BigDecimal DEFAULT_MARGIN = BigDecimal.ZERO;

    private final Map<String, BigDecimal> userMargins;
    private final int priceScale;

    public MarginService(
            @Value("#{${shop.catalog.pricing.user-margins:{}}}") Map<String, BigDecimal> userMargins,
            @Value("${shop.catalog.pricing.price-scale:2}") int priceScale) {
        this.userMargins = userMargins;
        this.priceScale = priceScale;
    }

    /**
     * Applies user-specific margin to the given base price.
     *
     * <p>Margin is expressed as a fraction added to the base price, e.g. 0.10 means +10%.
     * When userId is null, blank or not mapped, the base price is returned unchanged.</p>
     *
     * @param basePrice base product price
     * @param userId    identifier provided by the gateway (X-User-Id)
     * @return price with applied margin, never null
     */
    public BigDecimal applyMargin(BigDecimal basePrice, String userId) {
        if (basePrice == null) {
            return null;
        }
        return marginFor(userId)
                .map(basePrice::multiply)
                .map(price -> price.setScale(priceScale, RoundingMode.HALF_UP))
                .orElseGet(() -> basePrice.setScale(priceScale, RoundingMode.HALF_UP));
    }

    /**
     * Returns the margin fraction configured for the given user, or empty when none.
     */
    public Optional<BigDecimal> marginFor(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(userMargins.get(userId.trim())).or(() -> Optional.of(DEFAULT_MARGIN));
    }
}
