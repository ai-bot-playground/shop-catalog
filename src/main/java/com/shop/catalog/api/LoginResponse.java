package com.shop.catalog.api;

import java.util.UUID;

public record LoginResponse(
        UUID sessionId,
        String username,
        java.math.BigDecimal margin
) {
}
