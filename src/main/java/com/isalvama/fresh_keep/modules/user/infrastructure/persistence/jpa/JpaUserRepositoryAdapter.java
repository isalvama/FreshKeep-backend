package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.user.application.port.out.UserRepositoryPort;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.mapper.UserMapper;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.spring_data_repository.JpaUserSpringDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserRepositoryAdapter implements UserRepositoryPort {
    private final JpaUserSpringDataRepository jpaUserSpringDataRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaUserSpringDataRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public void save(User user) {
        jpaUserSpringDataRepository.save(userMapper.toEntity(user));
    }
}
