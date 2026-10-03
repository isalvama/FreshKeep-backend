package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ProductMetricResponse(
        @Schema(description = "Day this count covers") LocalDate date,
        @Schema(description = "Count for that day") long count
) {
}
