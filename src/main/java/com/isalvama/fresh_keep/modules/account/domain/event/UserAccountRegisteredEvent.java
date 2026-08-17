package com.isalvama.fresh_keep.modules.account.domain.event;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;

import java.util.UUID;

public record UserAccountRegisteredEvent(
        UUID accountId,
        String email
) {

    public static UserAccountRegisteredEvent from (Account account){
        return new UserAccountRegisteredEvent(
                account.getId().value(),
                account.getEmail().toString()
                );
    }
}
