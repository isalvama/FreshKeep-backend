package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.admin.application.port.out.AdminRepositoryPort;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.admin.infrastructure.exception.AdminPersistenceException;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaAdminRepositoryAdapter implements AdminRepositoryPort {
    private final JpaAdminSpringDataRepository jpaAdminSpringDataRepository;
    private final AdminMapper adminMapper;

    @Override
    public Optional<Admin> findByEmail(String email) {
        try{
            return jpaAdminSpringDataRepository.findByEmail(email).map(adminMapper::toDomain);
        } catch (DataAccessException e) {
            throw new AdminPersistenceException(
                    "Failed to retrieve admin data with email " + email + ": " + e.getMessage());
        }
    }

    @Override
    public Optional<Admin> findByAccountId(UUID accountId) {
        try {
            return jpaAdminSpringDataRepository.findByAccountId(accountId).map(adminMapper::toDomain);
        } catch (DataAccessException e) {
            throw new AdminPersistenceException(
                    "Failed to retrieve admin data with id " + accountId.toString() + ": " + e.getMessage());
        }
    }

    @Override
    public void save(Admin admin) {
        try {
            jpaAdminSpringDataRepository.save(adminMapper.toEntity(admin));
        } catch (DataAccessException e) {
            throw new AdminPersistenceException(
                    "Failed to persist admin with id " + admin.getId().toString() + ": " + e.getMessage());
        }
    }
}
