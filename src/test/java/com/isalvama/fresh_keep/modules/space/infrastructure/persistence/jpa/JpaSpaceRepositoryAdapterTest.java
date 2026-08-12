package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.StorageSpotsMapper;
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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpaceMapper.class, StorageSpotsMapper.class, JpaSpaceRepositoryAdapter.class})
class JpaSpaceRepositoryAdapterTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaSpaceRepositoryAdapter adapter;

    @Autowired
    private SpaceSpringDataRepository spaceSpringDataRepository;

    private AccountId creatorAccountId;
    private AccountId participantAccountId;
    private UserId participantUserId;
    private UserId creatorUserId;

    @BeforeEach
    void setUp(){
        creatorAccountId = AccountId.generate();
        creatorUserId = UserId.create();
        participantAccountId = AccountId.generate();
        participantUserId = UserId.create();
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                creatorAccountId.value(), "creator@email.com", "hf9843hf3fi8h"
        );
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                participantAccountId.value(), "participant@email.com", "eugf23gfi"
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, username) VALUES (?, ?, ?)",
                creatorUserId.value(), creatorAccountId.value(), "testUserName"
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, username) VALUES (?, ?, ?)",
                participantUserId.value(), participantAccountId.value(), "testUserName"
        );
    }

    @Test
    void save_shouldPersistSpaceAndItsRelationsInRealPostgres() {
        SpaceId spaceId = SpaceId.create();

        StorageSpot spot = StorageSpot.create(
                StorageSpotName.from("Main Freezer"),
                StorageSpotType.FREEZER
        );

        Space space = Space.reconstitute(
                spaceId,
                SpaceName.from("My House"),
                Set.of(spot),
                creatorUserId,
                Set.of(creatorUserId, participantUserId)
        );

        adapter.save(space);
        spaceSpringDataRepository.flush();


        JpaSpaceEntity saved = spaceSpringDataRepository.findById(spaceId.value()).orElseThrow();

        assertEquals(spaceId.value(), saved.getId());
        assertEquals("My House", saved.getName());
        assertEquals(creatorUserId.value(), saved.getCreatorId());

        assertEquals(1, saved.getStorageSpots().size());
        assertEquals("Main Freezer", saved.getStorageSpots().iterator().next().getName());
        assertEquals(StorageSpotType.FREEZER, saved.getStorageSpots().iterator().next().getType());

        assertEquals(2, saved.getParticipantIds().size());
        assertTrue(saved.getParticipantIds().contains(creatorUserId.value()));

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
        assertTrue(saved.getCreatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
        assertNotNull(saved.getLastUpdatedAt());
        assertTrue(saved.getLastUpdatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
        assertTrue(saved.getLastUpdatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
    }
}