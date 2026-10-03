package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DailyReceiptSummaryResponse(
        @Schema(description = "Day this count covers") LocalDate date,
        @Schema(description = "Total shopping receipts created that day") long totalReceipts
) {
}
