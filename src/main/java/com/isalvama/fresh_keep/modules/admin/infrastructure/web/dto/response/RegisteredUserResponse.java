package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public record RegisteredUserResponse(
        @Schema(description = "User id") UUID id,
        @Schema(description = "Account email") String email,
        @Schema(description = "Username") String username,
        @Schema(description = "Registration timestamp") Instant registeredAt,
        @Schema(description = "Last login timestamp, if any") Instant lastLoggedAt
) {
}
