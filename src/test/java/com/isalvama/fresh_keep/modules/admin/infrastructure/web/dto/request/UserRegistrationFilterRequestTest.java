package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidMetricsDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserRegistrationFilterRequestTest {

    @Test
    void acceptsDateRangeOfOneHundredDays() {
        assertDoesNotThrow(() -> new UserRegistrationFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 11)));
    }

    @Test
    void rejectsDateRangeLongerThanOneHundredDays() {
        assertThrows(InvalidMetricsDateRangeException.class, () -> new UserRegistrationFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 12)));
    }

    @Test
    void rejectsReversedDateRange() {
        assertThrows(InvalidMetricsDateRangeException.class, () -> new UserRegistrationFilterRequest(
                LocalDate.of(2026, 4, 11), LocalDate.of(2026, 1, 1)));
    }
}
