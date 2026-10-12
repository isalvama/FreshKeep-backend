package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserQueryAdapter.class, UserQueryBuilder.class,
        com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.user_query.mapper.UserQueryMapper.class,
        com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.query_builder.MetricsQueryBuilder.class})
class UserQueryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    private static final LocalDate SEP_10 = LocalDate.of(2026, 9, 10);

    @Autowired
    private UserQueryAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findByUserId_isEmptyForAnUnknownUser() {
        assertTrue(adapter.findByUserId(UUID.randomUUID()).isEmpty());
    }

    @Test
    void findByUserId_returnsRolesSpaceParticipationAndReceipts() {
        UUID aliceId = insertUser("alice@email.com", "alice", Set.of("USER"));

        // Alice created this space but was never added as a participant - must NOT show up: the
        // requirement is "spaces this user participates in", not "spaces this user created".
        UUID createdButNotJoined = insertSpace("Garage", aliceId);

        // Alice participates in this one, created by someone else entirely - must show up.
        UUID bobId = insertUser("bob@email.com", "bob", Set.of("USER"));
        UUID participatesIn = insertSpace("Kitchen", bobId);
        addParticipant(participatesIn, aliceId);

        UUID receiptId = UUID.randomUUID();
        jdbcTemplate.update(
                "INSERT INTO shopping_receipts (id, creator_id, space_id, purchase_date, created_at, store_name, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, 'DRAFT')",
                receiptId, aliceId, participatesIn, Timestamp.valueOf(SEP_10.atTime(12, 0)),
                Timestamp.valueOf(SEP_10.atTime(12, 0)), "SuperMart");

        UserDetailsDto details = adapter.findByUserId(aliceId).orElseThrow();

        assertEquals(aliceId, details.id());
        assertEquals("alice@email.com", details.email());
        assertEquals("alice", details.username());
        assertNotNull(details.registeredAt());
        assertEquals(List.of("USER"), details.roles());

        assertEquals(List.of(participatesIn), details.spaces().stream().map(UserDetailsDto.UserSpaceDto::id).toList());
        assertFalse(details.spaces().stream().anyMatch(s -> s.id().equals(createdButNotJoined)));

        assertEquals(1, details.receipts().size());
        assertEquals(receiptId, details.receipts().getFirst().id());
        assertEquals("SuperMart", details.receipts().getFirst().storeName());
    }

    @Test
    void findByUserId_returnsEmptyListsNotNullEntriesWhenUserHasNoSpacesOrReceipts() {
        UUID aliceId = insertUser("alice@email.com", "alice", Set.of("USER"));

        UserDetailsDto details = adapter.findByUserId(aliceId).orElseThrow();

        assertEquals(List.of(), details.spaces());
        assertEquals(List.of(), details.receipts());
    }

    @Test
    void findByUserId_returnsAllRolesWhenAccountHasMultiple() {
        UUID adminId = insertUser("root@email.com", "root", Set.of("USER", "ADMIN"));

        UserDetailsDto details = adapter.findByUserId(adminId).orElseThrow();

        assertEquals(Set.of("USER", "ADMIN"), Set.copyOf(details.roles()));
    }

    private UUID insertUser(String email, String username, Set<String> roles) {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, 'hash')", accountId, email);
        for (String role : roles) {
            jdbcTemplate.update("INSERT INTO roles (account_id, role) VALUES (?, ?)", accountId, role);
        }
        jdbcTemplate.update("INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                userId, accountId, email, username);
        return userId;
    }

    private UUID insertSpace(String name, UUID creatorId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO spaces (id, name, emoji, creator_id) VALUES (?, ?, '🏠', ?)", id, name, creatorId);
        return id;
    }

    private void addParticipant(UUID spaceId, UUID participantId) {
        jdbcTemplate.update("INSERT INTO spaces_participants (participant_id, space_id) VALUES (?, ?)", participantId, spaceId);
    }
}
