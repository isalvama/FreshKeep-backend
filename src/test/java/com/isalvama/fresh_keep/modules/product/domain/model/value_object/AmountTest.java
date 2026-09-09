package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountTest {

    @Test
    void of_bigDecimal_scalesValueToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("2.50"), Amount.of(new BigDecimal("2.5")).value());
    }

    @Test
    void of_bigDecimal_roundsHalfUpWhenMoreThanTwoDecimalPlaces() {
        assertEquals(new BigDecimal("2.46"), Amount.of(new BigDecimal("2.455")).value());
    }

    @Test
    void of_double_createsAmountScaledToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("3.14"), Amount.of(3.14).value());
    }

    @Test
    void of_string_createsAmountScaledToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("1.20"), Amount.of("1.2").value());
    }

    @Test
    void of_unitsAndDecimals_combinesThemIntoASingleAmount() {
        assertEquals(new BigDecimal("5.75"), Amount.of(5, 75).value());
    }

    @Test
    void of_throwsInvalidAmountExceptionWhenValueIsNull() {
        assertThrows(InvalidAmountException.class, () -> Amount.of((BigDecimal) null));
    }

    @Test
    void of_throwsInvalidAmountExceptionWhenValueIsNegative() {
        assertThrows(InvalidAmountException.class, () -> Amount.of(-1.0));
    }

    @Test
    void of_doesNotThrowWhenValueIsZero() {
        assertDoesNotThrow(() -> Amount.of(0));
    }

    @Test
    void zero_isScaledToTwoDecimalPlaces() {
        assertEquals(new BigDecimal("0.00"), Amount.ZERO.value());
    }

    @Test
    void toString_returnsPlainStringRepresentationOfValue() {
        assertEquals("2.50", Amount.of(2.5).toString());
    }

    @Test
    void equals_returnsTrueForAmountsWithSameScaledValue() {
        assertEquals(Amount.of("2.5"), Amount.of(2.50));
    }
}
