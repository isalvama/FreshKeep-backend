package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account;

import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaAccountRepositoryAdapter implements AccountRepositoryPort {
    private final AccountSpringDataRepository accountSpringDataRepository;
    private final AccountMapper accountMapper;

    @Override
    public Optional<Account> findByEmail(String email) {
        return accountSpringDataRepository.findByEmail(email).map(accountMapper::toDomain);
    }

    @Override
    public Account save(Account account) {
        JpaAccountEntity savedEntity = accountSpringDataRepository.save(accountMapper.toEntity(account));
        return accountMapper.toDomain(savedEntity);
    }
}
