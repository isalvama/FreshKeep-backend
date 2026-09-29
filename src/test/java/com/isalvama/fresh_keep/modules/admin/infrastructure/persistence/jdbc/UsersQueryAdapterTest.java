package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jdbc;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUsersPageDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UsersQueryAdapter.class, QueryAppender.class})
class UsersQueryAdapterTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private UsersQueryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QueryAppender queryAppender;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = insertUser("user@email.com", "user", LocalDate.of(2026, 1, 10));
    }

    @Test
    void getUserRegistrations_groupsUsersByRegistrationDate() {
        insertUser("second@email.com", "second", LocalDate.of(2026, 1, 10));
        insertUser("third@email.com", "third", LocalDate.of(2026, 1, 11));

        var result = adapter.getUserRegistrations(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11));

        assertEquals(List.of(LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11)), result.stream().map(dto -> dto.date()).toList());
        assertEquals(List.of(2L, 1L), result.stream().map(dto -> dto.count()).toList());
    }

    @Test
    void findRegisteredUsers_appliesDateRangePaginationAndTotalCount() {
        insertUser("second@email.com", "second", LocalDate.of(2026, 1, 10));
        insertUser("third@email.com", "third", LocalDate.of(2026, 1, 11));

        RegisteredUsersPageDto result = adapter.findRegisteredUsers(
                LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 11), 1, 1);

        assertEquals(1, result.content().size());
        assertEquals(3, result.totalElements());
        assertTrue(List.of("user@email.com", "second@email.com")
                .contains(result.content().getFirst().email()));
    }

    @Test
    void findByUserId_returnsDetailsWithSpacesAndReceipts() {
        UUID spaceId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO spaces (id, name, emoji) VALUES (?, ?, ?)", spaceId, "Kitchen", "🏠");
        jdbcTemplate.update("INSERT INTO spaces_participants (space_id, participant_id) VALUES (?, ?)", spaceId, userId);
        UUID receiptId = UUID.randomUUID();
        Timestamp createdAt = Timestamp.valueOf("2026-01-12 10:00:00");
        jdbcTemplate.update("INSERT INTO shopping_receipts (id, creator_id, created_at, purchase_date, store_name, status) VALUES (?, ?, ?, ?, ?, 'DRAFT')",
                receiptId, userId, createdAt, createdAt, "Store");

        UserDetailsDto result = adapter.findByUserId(userId).orElseThrow();

        assertEquals(userId, result.id());
        assertEquals("Kitchen", result.spaces().getFirst().name());
        assertEquals(receiptId, result.receipts().getFirst().id());
    }

    @Test
    void findByUserId_returnsEmptyForMissingUser() {
        assertTrue(adapter.findByUserId(UUID.randomUUID()).isEmpty());
    }

    private UUID insertUser(String email, String username, LocalDate date) {
        UUID accountId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        Timestamp createdAt = Timestamp.valueOf(date.atStartOfDay().plusHours(10));
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash, created_at) VALUES (?, ?, ?, ?)", accountId, email, "hash", createdAt);
        jdbcTemplate.update("INSERT INTO users (id, account_id, email, username, created_at) VALUES (?, ?, ?, ?, ?)", id, accountId, email, username, createdAt);
        return id;
    }
}
