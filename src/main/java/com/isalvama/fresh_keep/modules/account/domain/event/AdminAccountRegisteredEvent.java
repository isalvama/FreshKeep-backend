package com.isalvama.fresh_keep.modules.account.domain.event;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;

import java.util.UUID;

public record AdminAccountRegisteredEvent(
        UUID accountId,
        String email
) {

    public static AdminAccountRegisteredEvent from (Account account){
        return new AdminAccountRegisteredEvent(
                account.getId().value(),
                account.getEmail().toString()
        );
    }
}
