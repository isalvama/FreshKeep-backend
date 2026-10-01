package com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidShoppingReceiptsDateRangeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShoppingReceiptsFilterRequestTest {
    @Test
    void acceptsOneHundredDayRange() {
        assertDoesNotThrow(() -> new ShoppingReceiptsFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 11), null, null, null, null));
    }

    @Test
    void defaultsToTheFirstPageOf30() {
        ShoppingReceiptsFilterRequest request = new ShoppingReceiptsFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), null, null, null, null);

        assertEquals(1, request.page());
        assertEquals(30, request.size());
    }

    @Test
    void rejectsRangeLongerThanOneHundredDays() {
        assertThrows(InvalidShoppingReceiptsDateRangeException.class, () -> new ShoppingReceiptsFilterRequest(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 4, 12), null, null, null, null));
    }
}
