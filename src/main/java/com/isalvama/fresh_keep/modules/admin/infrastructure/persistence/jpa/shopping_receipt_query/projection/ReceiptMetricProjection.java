package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection;

import java.time.LocalDate;

public record ReceiptMetricProjection(
        LocalDate date,
        long totalReceipts
) {
}
