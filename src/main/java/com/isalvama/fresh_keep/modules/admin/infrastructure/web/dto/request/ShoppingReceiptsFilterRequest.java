package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidShoppingReceiptsDateRangeException;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ShoppingReceiptsFilterRequest(
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Required. Range start (inclusive)")
        LocalDate from,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Required. Range end (inclusive); must span 0-100 days from 'from'")
        LocalDate to,
        @RequestParam(required = false)
        @Schema(description = "Optional space filter")
        UUID spaceId,
        @RequestParam(required = false)
        @Schema(description = "Optional creator filter")
        UUID userId,
        @Min(1)
        @Schema(description = "1-based page number", defaultValue = "1")
        Integer page,
        @Positive @Max(40)
        @Schema(description = "Page size, maximum 40", defaultValue = "30")
        Integer size
) {
    public ShoppingReceiptsFilterRequest {
        if (page == null) page = 1;
        if (size == null) size = 30;
        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(from, to);
            if (days < 0 || days > 100) {
                throw new InvalidShoppingReceiptsDateRangeException("from and to must span between 0 and 100 days");
            }
        }
    }
}
