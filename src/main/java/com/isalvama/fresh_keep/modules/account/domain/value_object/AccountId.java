package com.isalvama.fresh_keep.modules.account.domain.value_object;
import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountIdException;

import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        if (value == null) throw new IllegalArgumentException("AccountId cannot be null");
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID());
    }
    public static AccountId of(UUID value) {return new AccountId(value);}

    public static AccountId from(String value) {
        try {
            return new AccountId(UUID.fromString(value));
        } catch (IllegalArgumentException e) {
            throw new InvalidAccountIdException("Invalid AccountId format of String " + value + ". " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
