package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidMoneyException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void constructor_throwsInvalidMoneyExceptionWhenAmountIsNull() {
        Exception exception = assertThrows(InvalidMoneyException.class, () -> new Money(null, Currency.USD));
        assertTrue(exception.getMessage().contains("amount"));
    }

    @Test
    void constructor_throwsInvalidMoneyExceptionWhenCurrencyIsNull() {
        Exception exception = assertThrows(InvalidMoneyException.class, () -> new Money(Amount.ZERO, null));
        assertTrue(exception.getMessage().contains("currency"));
    }

    @Test
    void getCurrencyName_returnsCurrencyDisplayName() {
        Money money = new Money(Amount.of(10), Currency.EUR);

        assertEquals("Euro", money.getCurrencyName());
    }

    @Test
    void getCurrencySymbol_returnsCurrencySymbol() {
        Money money = new Money(Amount.of(10), Currency.EUR);

        assertEquals("€", money.getCurrencySymbol());
    }

    @Test
    void from_createsMoneyFromBigDecimalAndCurrencyConstantName() {
        Money money = Money.from(new BigDecimal("9.99"), "USD");

        assertEquals(Amount.of("9.99"), money.amount());
        assertEquals(Currency.USD, money.currency());
    }

    @Test
    void from_throwsIllegalArgumentExceptionWhenCurrencyNameIsUnknown() {
        assertThrows(IllegalArgumentException.class, () -> Money.from(BigDecimal.ONE, "XYZ"));
    }

    @Test
    void equals_returnsTrueForMoneyWithSameAmountAndCurrency() {
        assertEquals(new Money(Amount.of(10), Currency.USD), new Money(Amount.of(10), Currency.USD));
    }
}
