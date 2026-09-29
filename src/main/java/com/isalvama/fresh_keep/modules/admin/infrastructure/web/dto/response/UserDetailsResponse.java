package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserDetailsResponse(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt,
        List<String> roles,
        List<SpaceSummary> spaces,
        List<ReceiptSummary> receipts
) {
    public record SpaceSummary(UUID id, String name) {
    }

    public record ReceiptSummary(UUID id, Instant createdAt, LocalDate purchaseDate, String storeName) {
    }
}
