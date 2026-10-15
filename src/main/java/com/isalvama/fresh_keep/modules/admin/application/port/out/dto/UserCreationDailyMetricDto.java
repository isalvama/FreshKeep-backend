package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.time.LocalDate;

public record UserCreationDailyMetricDto(
        LocalDate date,
        long count
) {
}
