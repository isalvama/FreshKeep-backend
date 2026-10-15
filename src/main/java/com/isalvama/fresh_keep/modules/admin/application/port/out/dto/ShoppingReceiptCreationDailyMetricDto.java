package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.LocalDate;

public record ShoppingReceiptCreationDailyMetricDto(
        LocalDate date,
        long totalReceipts
) {
}
