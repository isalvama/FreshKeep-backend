package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidMoneyException;

import java.math.BigDecimal;

public record Money (Amount amount, Currency currency){
    public Money {
        validateNotNull(amount, "amount");
        validateNotNull(currency, "currency");
    }

    public static <T> void validateNotNull (T object, String fieldName){
        if (object == null){
            throw new InvalidMoneyException(fieldName + " cannot be null.");
        }
    }

    public String getCurrencyName(){
        return this.currency.getDisplayName();
    }

    public String getCurrencySymbol(){
        return this.currency.getSymbol();
    }

    public static Money from (BigDecimal amount, String currencyConstName){
        return new Money(Amount.of(amount), Currency.valueOf(currencyConstName));
    }

    public static Money from (BigDecimal amount, Currency currency){
        return new Money(Amount.of(amount), currency);
    }
}
