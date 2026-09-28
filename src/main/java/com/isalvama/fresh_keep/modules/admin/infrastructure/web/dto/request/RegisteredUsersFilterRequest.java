package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidRegisteredUsersDateRangeException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record RegisteredUsersFilterRequest(
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,
        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to,
        @Min(1)
        Integer page,
        @Positive @Max(40)
        Integer size
) {
    public RegisteredUsersFilterRequest {
        if (page == null) page = 1;
        if (size == null) size = 30;
        if (from != null && to != null) {
            long days = ChronoUnit.DAYS.between(from, to);
            if (days < 0 || days > 90) {
                throw new InvalidRegisteredUsersDateRangeException("from and to must span between 0 and 90 days");
            }
        }
    }
}
