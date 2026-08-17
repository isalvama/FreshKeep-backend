package com.isalvama.fresh_keep.modules.user.application.port.out;

import com.isalvama.fresh_keep.modules.user.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByEmail(String email);

    void save(User user);
}
