package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.LocalDate;

public record DailyReceiptSummaryDto(
        LocalDate date,
        long totalReceipts
) {
}
