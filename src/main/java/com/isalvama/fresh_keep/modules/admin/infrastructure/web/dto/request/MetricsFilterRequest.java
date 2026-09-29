package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidMetricsDateRangeException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record MetricsFilterRequest(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,

        @RequestParam(required = false)
        UUID userId,

        @Positive @Max(40)
        Integer size,

        @Min(0)
        Integer page
) {
    public MetricsFilterRequest {
        if (size == null) size = 30;
        if (page == null) page = 0;

        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(from, to);
            if (days < 0 || days > 100) {
                throw new InvalidMetricsDateRangeException("from and to must span between 0 and 100 days");
            }
        }
    }
}
