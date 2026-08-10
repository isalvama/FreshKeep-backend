package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.entity.JpaAccountEntity;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.mapper.AccountMapper;
import com.isalvama.fresh_keep.shared.domain.Role;
import org.junit.jupiter.api.DisplayName;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({JpaAccountRepositoryAdapter.class, AccountMapper.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class JpaAccountRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static org.testcontainers.postgresql.PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JpaAccountRepositoryAdapter adapter;

    @Autowired
    private AccountSpringDataRepository springRepository;

    @Test
    @DisplayName("Should update last login timestamp in database")
    void saveAccount_Success() {
        // 1. GIVEN:
        UUID id = UUID.randomUUID();
        String email = "test@test.com";
        String passwordHash = "hash";
        Role user = Role.USER;

        Account account = Account.reconstitute(AccountId.of(id), Email.of(email), passwordHash, Set.of(user));

        // 2. WHEN
        adapter.save(account);
        springRepository.flush();

        // 3. THEN:
        Account savedAccount = adapter.findByEmail(email).orElseThrow();
        assertEquals(id, savedAccount.getId().value());
        assertEquals(email, savedAccount.getEmail().value());
        assertEquals(passwordHash, savedAccount.getPasswordHash());
        assertEquals(1, savedAccount.getRoles().size());
        assertTrue(savedAccount.getRoles().contains(user));

        JpaAccountEntity created = springRepository.findById(id).orElseThrow();
        assertNotNull(created.getCreatedAt());
        assertTrue(created.getCreatedAt().isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)));
        assertTrue(created.getCreatedAt().isBefore(Instant.now()));

        assertNull(created.getLastLogIn());
    }

    @Test
    @DisplayName("Should update last login timestamp in database")
    void updateLastLogin_Success() {
        // 1. GIVEN:
        UUID id = UUID.randomUUID();
        Instant initialLogin = Instant.now().minus(1, ChronoUnit.DAYS);

        JpaAccountEntity entity = JpaAccountEntity.builder()
                .id(id)
                .email("test@test.com")
                .passwordHash("hash")
                .roles(Set.of(Role.USER))
                .lastLogIn(initialLogin)
                .build();
        springRepository.saveAndFlush(entity);

        // 2. WHEN
        Instant newLogin = Instant.now();
        adapter.updateLastLogIn(AccountId.of(id), newLogin);
        springRepository.flush();

        // 3. THEN:
        JpaAccountEntity updated = springRepository.findById(id).orElseThrow();
        assertEquals(newLogin.truncatedTo(ChronoUnit.MILLIS),
                updated.getLastLogIn().truncatedTo(ChronoUnit.MILLIS));
    }
}