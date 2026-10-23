package com.isalvama.fresh_keep.modules.product.application.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductMove(
        String userId,
        String newStorageSpotId,
        LocalDateTime changedAt,
        LocalDate newExpirationDate
) {
}
