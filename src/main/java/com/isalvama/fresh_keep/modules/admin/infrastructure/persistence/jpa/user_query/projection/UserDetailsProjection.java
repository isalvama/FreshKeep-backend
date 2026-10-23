package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.projection;

import java.time.Instant;
import java.util.UUID;

public record UserDetailsProjection(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt
) {
}
