package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection;

import java.time.LocalDate;

public record MetricProjection(
        LocalDate date,
        long totalReceipts
) {
}
