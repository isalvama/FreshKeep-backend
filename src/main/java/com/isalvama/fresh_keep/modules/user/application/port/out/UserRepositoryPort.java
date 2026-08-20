package com.isalvama.fresh_keep.modules.user.application.port.out;

import com.isalvama.fresh_keep.modules.user.domain.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    Optional<User> findByEmail(String email);

    Optional<User> findByAccountId(UUID accountId);

    void save(User user);
}
