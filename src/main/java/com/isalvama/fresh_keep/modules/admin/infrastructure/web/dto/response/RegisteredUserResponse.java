package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.time.Instant;
import java.util.UUID;

public record RegisteredUserResponse(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt
) {
}
