package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserDetailsResult(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt,
        List<String> roles,
        List<SpaceResult> spaces,
        List<ReceiptResult> receipts
) {
    public record SpaceResult(UUID id, String name) {
    }

    public record ReceiptResult(UUID id, Instant createdAt, LocalDate purchaseDate, String storeName) {
    }
}
