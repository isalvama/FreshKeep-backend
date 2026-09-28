package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.LocalDate;

public record DailyMetricDto(
        LocalDate date,
        long count
) {
}
