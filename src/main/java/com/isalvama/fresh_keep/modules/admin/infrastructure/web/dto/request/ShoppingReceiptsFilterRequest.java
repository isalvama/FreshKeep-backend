package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidShoppingReceiptsDateRangeException;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public record ShoppingReceiptsFilterRequest(
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,
        @RequestParam(required = false)
        UUID spaceId,
        @RequestParam(required = false)
        UUID userId
) {
    public ShoppingReceiptsFilterRequest {
        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(from, to);
            if (days < 0 || days > 100) {
                throw new InvalidShoppingReceiptsDateRangeException("from and to must span between 0 and 100 days");
            }
        }
    }
}
