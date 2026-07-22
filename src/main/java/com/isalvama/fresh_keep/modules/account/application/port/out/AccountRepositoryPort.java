package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;

import java.util.Optional;

public interface AccountRepositoryPort {
    Optional<Account> findByEmail(String email);
    Account save(Account account);
    }
