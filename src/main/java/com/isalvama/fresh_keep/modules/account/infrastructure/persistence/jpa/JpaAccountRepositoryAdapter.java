package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.entity.JpaAccountEntity;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.mapper.AccountMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.exception.SpacePersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaAccountRepositoryAdapter implements AccountRepositoryPort {
    private final AccountSpringDataRepository accountSpringDataRepository;
    private final AccountMapper accountMapper;

    @Override
    public Optional<Account> findByEmail(String email) {
        try {
        return accountSpringDataRepository.findByEmail(email).map(accountMapper::toDomain);
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to retrieve account data with email " + email + ": " + e.getMessage());
        }
    }

    @Override
    public Account save(Account account) {
        try {
        JpaAccountEntity savedEntity = accountSpringDataRepository.save(accountMapper.toEntity(account));
        return accountMapper.toDomain(savedEntity);
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to retrieve account data with id " + account.getId().toString() + ": " + e.getMessage());
        }
    }

    @Override
    public void updateLastLogIn(AccountId id, Instant now) {
        try {
            accountSpringDataRepository.updateLastLogIn(id.value(), now);
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to persist space with id " + id + ": " + e.getMessage());
        }
}
}
