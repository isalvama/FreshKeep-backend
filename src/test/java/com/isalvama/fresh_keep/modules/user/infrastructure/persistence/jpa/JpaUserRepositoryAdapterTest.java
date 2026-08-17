package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserName;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.mapper.UserMapper;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.spring_data_repository.JpaUserSpringDataRepository;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserMapper.class, JpaUserRepositoryAdapter.class})
class JpaUserRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaUserRepositoryAdapter adapter;

    @Autowired
    private JpaUserSpringDataRepository jpaUserSpringDataRepository;

    private AccountId account1Id;
    private UserId user1Id;
    private String user1Email;
    private String user1UserName;

    private AccountId account2Id;
    private UserId user2Id;
    private String user2Email;
    private String user2UserName;

    @BeforeEach
    void setUp(){
        account1Id = AccountId.create();
        user1Id = UserId.create();
        user1Email = "user1@email.com";
        user1UserName = "testUserName1";
        account2Id = AccountId.create();
        user2Id = UserId.create();
        user2Email = "user2@email.com";
        user2UserName = "testUserName2";
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                account1Id.value(), user1Email, "hf9843hf3fi8h"
        );
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                account2Id.value(), user2Email, "wyfh98243gfp192"
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                user1Id.value(), account1Id.value(), user1Email, user1UserName
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                user2Id.value(), account2Id.value(), user2Email, user2UserName
        );
    }

    @Test
    void findByEmail_shouldReturnUserDataWithMatchingEmail() {
        User user1 = adapter.findByEmail(user1Email).orElseThrow();

        assertEquals(user1Email, user1.getEmail().toString());
        assertEquals(user1Id, user1.getId());
        assertEquals(account1Id, user1.getAccountId());
        assertEquals(user1UserName.toLowerCase(), user1.getUserName().toString());

        User user2 = adapter.findByEmail(user2Email).orElseThrow();

        assertEquals(user2Email, user2.getEmail().toString());
        assertEquals(user2Id, user2.getId());
        assertEquals(account2Id, user2.getAccountId());
        assertEquals(user2UserName.toLowerCase(), user2.getUserName().toString());
    }

    @Test
    void findByEmail_shouldReturnEmptyOptional() {
        Optional<User> result = adapter.findByEmail("inexistant@mail.com");

        assertEquals(Optional.empty(), result);
    }

    @Test
    void findByAccountId_shouldReturnUserDataWithMatchingEmail() {
        User user1 = adapter.findByAccountId(account1Id.value()).orElseThrow();

        assertEquals(user1Email, user1.getEmail().toString());
        assertEquals(user1Id, user1.getId());
        assertEquals(account1Id, user1.getAccountId());
        assertEquals(user1UserName.toLowerCase(), user1.getUserName().toString());

        User user2 = adapter.findByAccountId(account2Id.value()).orElseThrow();

        assertEquals(user2Email, user2.getEmail().toString());
        assertEquals(user2Id, user2.getId());
        assertEquals(account2Id, user2.getAccountId());
        assertEquals(user2UserName.toLowerCase(), user2.getUserName().toString());
    }

    @Test
    void findByAccountId_shouldReturnEmptyOptional() {
        Optional<User> result = adapter.findByAccountId(UUID.randomUUID());

        assertEquals(Optional.empty(), result);
    }

    @Test
    void save_shouldPersistSpaceAndItsRelationsInRealPostgres() {
        UserId newlyCreatedUserId = UserId.create();
        AccountId newlyCreatedAccountId = AccountId.create();
        String newlyCreatedEmail = "newuser@mail.com";
        String userName = "new.user.username";

        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                newlyCreatedAccountId.value(), newlyCreatedEmail, "wyfh98243gfp192"
        );

        User user = User.reconstitute(newlyCreatedUserId, newlyCreatedAccountId, Email.of(newlyCreatedEmail), UserName.of(userName));

        adapter.save(user);

        jpaUserSpringDataRepository.flush();

        JpaUserEntity persistedUserEntity = jpaUserSpringDataRepository.findByEmail(newlyCreatedEmail).orElseThrow();

        assertEquals(newlyCreatedEmail, persistedUserEntity.getEmail());
        assertEquals(newlyCreatedUserId.value(), persistedUserEntity.getId());
        assertEquals(newlyCreatedAccountId.value(), persistedUserEntity.getAccountId());
        assertEquals(userName.toLowerCase(), persistedUserEntity.getUserName());

        assertNotNull(persistedUserEntity.getCreatedAt());
        assertTrue(persistedUserEntity.getCreatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
        assertTrue(persistedUserEntity.getCreatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
        assertNotNull(persistedUserEntity.getLastUpdatedAt());
        assertTrue(persistedUserEntity.getLastUpdatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
        assertTrue(persistedUserEntity.getLastUpdatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
    }
}