package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.exception.SpacePersistenceException;
import com.isalvama.fresh_keep.modules.user.application.port.out.UserRepositoryPort;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.mapper.UserMapper;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.spring_data_repository.JpaUserSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {
    private final JpaUserSpringDataRepository jpaUserSpringDataRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        try{
            return jpaUserSpringDataRepository.findByEmail(email).map(userMapper::toDomain);
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to retrieve user data with email " + email + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<User> findByAccountId(UUID accountId) {
        try {
            return jpaUserSpringDataRepository.findByAccountId(accountId).map(userMapper::toDomain);
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to retrieve account data with id " + accountId.toString() + ": " + e.getMessage());
        }
    }

    @Override
    public void save(User user) {
        try{
        jpaUserSpringDataRepository.save(userMapper.toEntity(user));
        } catch (DataAccessException e) {
            throw new SpacePersistenceException(
                    "Failed to persist user with id " + user.getId().toString() + ": " + e.getMessage());
        }
    }
}
