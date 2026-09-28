package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidRegisteredUsersDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegisteredUsersFilterRequestTest {
    @Test
    void acceptsNinetyDayRange() {
        assertDoesNotThrow(() -> new RegisteredUsersFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 1), 1, 40));
    }

    @Test
    void rejectsRangeLongerThanNinetyDays() {
        assertThrows(InvalidRegisteredUsersDateRangeException.class, () -> new RegisteredUsersFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 2), 1, 40));
    }
}
