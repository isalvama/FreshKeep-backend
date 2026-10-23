package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidMetricsDateRangeException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ProductMetricsFilterRequest(
        @RequestParam
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Required. Range start (inclusive)")
        LocalDate from,

        @RequestParam
        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Required. Range end (inclusive); must span 0-100 days from 'from'")
        LocalDate to,

        @RequestParam(required = false)
        @Schema(description = "Optional space filter")
        UUID spaceId,

        @RequestParam(required = false)
        @Schema(description = "Optional creator filter")
        UUID creatorId
) {
    public ProductMetricsFilterRequest {
        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(from, to);
            if (days < 0 || days > 100) {
                throw new InvalidMetricsDateRangeException("from and to must span between 0 and 100 days");
            }
        }
    }
}
