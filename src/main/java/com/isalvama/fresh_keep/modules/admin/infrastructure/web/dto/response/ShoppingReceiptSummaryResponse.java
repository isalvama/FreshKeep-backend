package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShoppingReceiptSummaryResponse(
        @Schema(description = "Shopping receipt id") UUID id,
        @Schema(description = "userId of the receipt's creator") UUID creatorId,
        @Schema(description = "Creator's email, if the creator still exists") String creatorEmail,
        @Schema(description = "Creator's username, if the creator still exists") String creatorUsername,
        @Schema(description = "Id of the space this receipt belongs to") UUID spaceId,
        @Schema(description = "Space name, if the space still exists") String spaceName,
        @Schema(description = "Store name") String storeName,
        @Schema(description = "Purchase date") LocalDate purchaseDate,
        @Schema(description = "Receipt creation timestamp") Instant createdAt,
        @Schema(description = "Number of non-deleted products on this receipt") long productCount
) {
}
