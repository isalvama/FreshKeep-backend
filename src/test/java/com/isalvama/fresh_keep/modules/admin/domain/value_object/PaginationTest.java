package com.isalvama.fresh_keep.modules.admin.domain.value_object;

import com.isalvama.fresh_keep.modules.admin.domain.exception.InvalidPaginationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaginationTest {

    @Test
    void fromPage_calculatesOffsetAndLimit() {
        Pagination pagination = Pagination.fromPage(3, 10);

        assertEquals(20, pagination.offset());
        assertEquals(10, pagination.limit());
    }

    @Test
    void fromPage_usesDefaultsWhenPageAndSizeAreNull() {
        Pagination pagination = Pagination.fromPage(null, null);

        assertEquals(0, pagination.offset());
        assertEquals(30, pagination.limit());
    }

    @Test
    void fromPage_normalizesInvalidPageAndSize() {
        Pagination pagination = Pagination.fromPage(0, 0);

        assertEquals(0, pagination.offset());
        assertEquals(30, pagination.limit());
    }

    @Test
    void fromPage_capsSizeAtMaximumLimit() {
        Pagination pagination = Pagination.fromPage(2, 100);

        assertEquals(40, pagination.offset());
        assertEquals(40, pagination.limit());
    }

    @Test
    void constructor_throwsWhenOffsetIsNegative() {
        assertThrows(InvalidPaginationException.class, () -> new Pagination(-1, 10));
    }

    @Test
    void constructor_throwsWhenLimitIsNotPositive() {
        assertThrows(InvalidPaginationException.class, () -> new Pagination(0, 0));
    }

    @Test
    void constructor_throwsWhenLimitExceedsMaximum() {
        assertThrows(InvalidPaginationException.class, () -> new Pagination(0, 41));
    }
}
