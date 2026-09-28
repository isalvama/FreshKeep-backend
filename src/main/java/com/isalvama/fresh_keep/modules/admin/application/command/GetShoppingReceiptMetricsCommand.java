package com.isalvama.fresh_keep.modules.admin.application.command;

import java.time.LocalDate;
import java.util.UUID;

public record GetShoppingReceiptMetricsCommand(
        LocalDate from,
        LocalDate to,
        UUID spaceId,
        UUID creatorId
) {
}
