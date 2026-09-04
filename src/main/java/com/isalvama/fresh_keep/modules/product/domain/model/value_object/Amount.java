package com.isalvama.fresh_keep.modules.product.domain.model.value_object;

import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Amount(BigDecimal value) {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static final Amount ZERO = new Amount(BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE));

    public Amount {
        validateNotNullAndNotNegative(value);
        value = value.setScale(SCALE, ROUNDING_MODE);
    }
    public static Amount of(BigDecimal value) {
        return new Amount(value);
    }

    public static Amount of(double value) {
        return new Amount(BigDecimal.valueOf(value));
    }

    public static Amount of(String value) {
        return new Amount(new BigDecimal(value));
    }

    public static Amount of(int units, int decimals) {
        return new Amount(BigDecimal.valueOf(units).add(BigDecimal.valueOf(decimals, SCALE)));
    }

    private void validateNotNullAndNotNegative (BigDecimal value){
        if (value == null) {
            throw new InvalidAmountException("amount cannot be null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0){
            throw new InvalidAmountException("amount cannot be negative");
        }
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}

