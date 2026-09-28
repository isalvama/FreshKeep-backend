package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import java.time.LocalDate;

public record ProductMetricResponse(
        LocalDate date,
        long count
) {
}
