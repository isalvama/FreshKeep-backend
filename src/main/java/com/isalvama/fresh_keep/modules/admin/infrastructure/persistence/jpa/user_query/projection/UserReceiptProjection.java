package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection;

import java.time.Instant;
import java.util.UUID;

public record (
        UUID id,
        Instant createdAt,
        Instant purchaseDate,
        String storeName
) {
}
