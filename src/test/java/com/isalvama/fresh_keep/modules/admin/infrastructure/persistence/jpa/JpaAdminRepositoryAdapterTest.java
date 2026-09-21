package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.entity.JpaAdminEntity;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.mapper.AdminMapper;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({AdminMapper.class, JpaAdminRepositoryAdapter.class})
class JpaAdminRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaAdminRepositoryAdapter adapter;

    @Autowired
    private JpaAdminSpringDataRepository springRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private UUID accountId;
    private UUID adminId;
    private String email;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        adminId = UUID.randomUUID();
        email = "admin@example.com";

        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                accountId, email, "password-hash");
    }

    @Test
    void findByEmail_shouldReturnMatchingAdmin() {
        insertAdmin();

        Admin admin = adapter.findByEmail(email).orElseThrow();

        assertEquals(adminId, admin.getId().value());
        assertEquals(accountId, admin.getAccountId().value());
        assertEquals(email, admin.getEmail().value());
    }

    @Test
    void findByEmail_shouldReturnEmptyWhenAdminDoesNotExist() {
        assertTrue(adapter.findByEmail("missing@example.com").isEmpty());
    }

    @Test
    void findByAccountId_shouldReturnMatchingAdmin() {
        insertAdmin();

        Admin admin = adapter.findByAccountId(accountId).orElseThrow();

        assertEquals(adminId, admin.getId().value());
        assertEquals(accountId, admin.getAccountId().value());
        assertEquals(email, admin.getEmail().value());
    }

    @Test
    void findByAccountId_shouldReturnEmptyWhenAdminDoesNotExist() {
        assertTrue(adapter.findByAccountId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void save_shouldPersistAdmin() {
        Admin admin = Admin.reconstitute(
                com.isalvama.fresh_keep.modules.admin.domain.value_object.AdminId.of(adminId),
                AccountId.of(accountId),
                Email.of(email));

        adapter.save(admin);
        springRepository.flush();

        JpaAdminEntity persisted = springRepository.findById(adminId).orElseThrow();

        assertEquals(adminId, persisted.getId());
        assertEquals(accountId, persisted.getAccountId());
        assertEquals(email, persisted.getEmail());
        assertNotNull(persisted.getCreatedAt());
    }

    private void insertAdmin() {
        jdbcTemplate.update(
                "INSERT INTO admins (id, account_id, email) VALUES (?, ?, ?)",
                adminId, accountId, email);
    }
}
