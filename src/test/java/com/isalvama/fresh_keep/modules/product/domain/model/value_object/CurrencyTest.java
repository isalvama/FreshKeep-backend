package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidCurrencyException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyTest {

    @Test
    void getValueOf_returnsMatchingConstantForValidName() {
        assertEquals(Currency.EUR, Currency.USD.getValueOf("EUR"));
    }

    @Test
    void getValueOf_returnsMatchingConstantForValidNameRegardlessOfCase() {
        assertEquals(Currency.EUR, Currency.USD.getValueOf("eUr"));
        assertEquals(Currency.EUR, Currency.USD.getValueOf("eur"));
        assertEquals(Currency.EUR, Currency.USD.getValueOf("Eur"));
    }

    @Test
    void getValueOf_throwsInvalidCurrencyExceptionForUnknownName() {
        Exception exception = assertThrows(InvalidCurrencyException.class, () -> Currency.USD.getValueOf("XYZ"));
        assertTrue(exception.getMessage().contains("XYZ"));
    }

    @Test
    void valueOf_returnsMatchingConstantForValidName() {
        assertEquals(Currency.USD, Currency.valueOf("USD"));
    }

    @Test
    void valueOf_throwsIllegalArgumentExceptionForUnknownName() {
        assertThrows(IllegalArgumentException.class, () -> Currency.valueOf("XYZ"));
    }

    @Test
    void getDisplayName_returnsExpectedDisplayName() {
        assertEquals("Euro", Currency.EUR.getDisplayName());
    }

    @Test
    void getSymbol_returnsExpectedSymbol() {
        assertEquals("€", Currency.EUR.getSymbol());
    }

    @Test
    void listValuesNames_returnsTheNameOfEveryConstant() {
        List<String> names = Currency.listValuesNames();

        assertEquals(Currency.values().length, names.size());
        assertTrue(names.contains("USD"));
        assertTrue(names.contains("EUR"));
        assertTrue(names.contains("JPY"));
    }
}
