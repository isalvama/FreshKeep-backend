package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;

import java.time.Instant;
import java.util.Optional;

public interface AccountRepositoryPort {
    Optional<Account> findByEmail(String email);
    Account save(Account account);
    void updateLastLogIn(AccountId id, Instant now);
    }
