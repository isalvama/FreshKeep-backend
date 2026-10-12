package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.Instant;
import java.util.UUID;

public record UserSummaryDto(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt
) {
}
