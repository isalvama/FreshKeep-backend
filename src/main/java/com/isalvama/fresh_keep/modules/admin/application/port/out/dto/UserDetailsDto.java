package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserDetailsDto(
        UUID id,
        String email,
        String username,
        Instant registeredAt,
        Instant lastLoggedAt,
        List<String> roles,
        List<UserSpaceDto> spaces,
        List<UserReceiptDto> receipts
) {
    public record UserSpaceDto(UUID id, String name) {
    }

    public record UserReceiptDto(
            UUID id,
            Instant createdAt,
            LocalDate purchaseDate,
            String storeName
    ) {
    }
}
