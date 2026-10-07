package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserDetailsResponse(
        @Schema(description = "User id") UUID id,
        @Schema(description = "Account email") String email,
        @Schema(description = "Username") String username,
        @Schema(description = "Registration timestamp") Instant registeredAt,
        @Schema(description = "Last login timestamp, if any") Instant lastLoggedAt,
        @Schema(description = "Account roles, e.g. [\"USER\"]") List<String> roles,
        @Schema(description = "Spaces this user participates in") List<SpaceSummary> spaces,
        @Schema(description = "Shopping receipts this user created") List<ReceiptSummary> receipts
) {
    public record SpaceSummary(
            @Schema(description = "Space id") UUID id,
            @Schema(description = "Space name") String name) {
    }

    public record ReceiptSummary(
            @Schema(description = "Receipt id") UUID id,
            @Schema(description = "Receipt creation timestamp") Instant createdAt,
            @Schema(description = "Receipt purchase date") LocalDate purchaseDate,
            @Schema(description = "Store name") String storeName) {
    }
}
