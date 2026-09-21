package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidInvitationCountException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountTest {

    @Test
    void of_returnsCountWithProvidedValue() {
        assertEquals(3, Count.of(3).value());
    }

    @Test
    void of_allowsZero() {
        assertEquals(0, Count.of(0).value());
    }

    @Test
    void of_throwsWhenValueIsNull() {
        assertThrows(InvalidInvitationCountException.class,
                () -> Count.of(null));
    }

    @Test
    void of_throwsWhenValueIsNegative() {
        assertThrows(InvalidInvitationCountException.class,
                () -> Count.of(-1));
    }

    @Test
    void add_returnsCountWithValueIncrementedByOne() {
        Count count = Count.of(2);

        Count result = count.add();

        assertEquals(3, result.value());
        assertEquals(2, count.value());
    }

    @Test
    void add_throwsWhenValueWouldOverflow() {
        assertThrows(ArithmeticException.class,
                () -> Count.of(Integer.MAX_VALUE).add());
    }
}
