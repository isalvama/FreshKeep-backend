package com.isalvama.fresh_keep.modules.admin.application.port.out;

import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;

import java.util.Optional;
import java.util.UUID;

public interface AdminRepositoryPort {
    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByAccountId(UUID accountId);

    void save(Admin admin);
}
