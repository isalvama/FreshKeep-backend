package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.time.Instant;
import java.util.UUID;

public record UserSummaryResult(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt
) {
}
