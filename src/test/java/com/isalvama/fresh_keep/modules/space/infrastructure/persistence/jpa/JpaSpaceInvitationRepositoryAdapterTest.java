package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.space.domain.model.SpaceInvitation;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Count;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceInvitationId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceInvitationEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceInvitationMapper;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaSpaceInvitationRepositoryAdapter.class, SpaceInvitationMapper.class})
class JpaSpaceInvitationRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaSpaceInvitationRepositoryAdapter adapter;

    @Autowired
    private JpaSpaceInvitationSpringDataRepository jpaRepository;

    private UUID spaceId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        UUID accountId = UUID.randomUUID();
        spaceId = UUID.randomUUID();
        userId = UUID.randomUUID();

        jdbcTemplate.update("INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                accountId, "invitation-owner@email.com", "password-hash");
        jdbcTemplate.update("INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                userId, accountId, "invitation-owner@email.com", "invitationOwner");
        jdbcTemplate.update("INSERT INTO spaces (id, name, emoji) VALUES (?, ?, ?)",
                spaceId, "Invitation Space", "🏠");
    }

    @Test
    void save_persistsInvitationWithItsCompleteState() {
        SpaceInvitation invitation = invitation(Count.of(5), Count.of(2));

        adapter.save(invitation);
        jpaRepository.flush();

        JpaSpaceInvitationEntity entity = jpaRepository.findById(invitation.getId().value()).orElseThrow();

        assertEquals(invitation.getId().value(), entity.getId());
        assertEquals(invitation.getToken().value(), entity.getToken());
        assertEquals(spaceId, entity.getSpaceId());
        assertEquals(userId, entity.getUserCreatorId());
        assertEquals(invitation.getExpiresAt().atZone(ZoneOffset.UTC).toInstant(), entity.getExpiresAt());
        assertTrue(entity.getIsActive());
        assertEquals(5, entity.getMaxUses());
        assertEquals(2, entity.getUsesCount());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void save_persistsNullMaximumUsesAndZeroUsesCount() {
        SpaceInvitation invitation = invitation(null, Count.of(0));

        adapter.save(invitation);
        jpaRepository.flush();

        JpaSpaceInvitationEntity entity = jpaRepository.findById(invitation.getId().value()).orElseThrow();

        assertNull(entity.getMaxUses());
        assertEquals(0, entity.getUsesCount());
    }

    private SpaceInvitation invitation(Count maxUses, Count usesCount) {
        SpaceInvitation template = new SpaceInvitation(
                SpaceInvitationId.create(), Token.create(), SpaceId.of(spaceId), UserId.of(userId),
                LocalDateTime.of(2026, 9, 20, 12, 30), true, Count.of(0));

        return template.reconstitute(
                template.getId(), template.getToken(), template.getSpaceId(), template.getUserCreatorId(),
                template.getExpiresAt(), template.getIsActive(), maxUses, usesCount);
    }
}
