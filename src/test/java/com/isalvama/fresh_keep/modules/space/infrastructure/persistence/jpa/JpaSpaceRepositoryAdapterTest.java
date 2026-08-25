package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.StorageSpotsMapper;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpaceMapper.class, StorageSpotsMapper.class, JpaSpaceRepositoryAdapter.class})
class JpaSpaceRepositoryAdapterTest {

    @BeforeEach
    void setUp(){
        creatorAccountId = AccountId.create();
        creatorUserId = UserId.create();
        creatorUserEmail = "creator@email.com";
        participantAccountId = AccountId.create();
        participantUserId = UserId.create();
        participantUserEmail = "participant@email.com";

        insertAccount(creatorAccountId.value(), creatorUserEmail, "hf9843hf3fi8h");
        insertAccount(participantAccountId.value(), participantUserEmail, "eugf23gfi");
        insertUser(creatorUserId.value(), creatorAccountId.value(), creatorUserEmail, "creatorUserName");
        insertUser(participantUserId.value(), participantAccountId.value(), participantUserEmail, "participantUserName");
    }

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaSpaceRepositoryAdapter adapter;

    @Autowired
    private JpaSpaceSpringDataRepository spaceSpringDataRepository;

    private AccountId creatorAccountId;
    private AccountId participantAccountId;
    private UserId participantUserId;
    private UserId creatorUserId;
    private String participantUserEmail;
    private String creatorUserEmail;

    @Nested
    class CreateSpace{

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
                    Emoji.from("🏠"),
                    Set.of(spot),
                    creatorUserId,
                    Set.of(creatorUserId, participantUserId)
            );

            adapter.save(space);
            spaceSpringDataRepository.flush();


            JpaSpaceEntity saved = spaceSpringDataRepository.findById(spaceId.value()).orElseThrow();

            assertEquals(spaceId.value(), saved.getId());
            assertEquals("My House", saved.getName());
            assertEquals("🏠", saved.getEmoji());
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

    @Nested
    class GetSpacesByParticipantId {

        private Space space1;
        private Space space2;
        private Space space3;
        private Space space4;


        @BeforeEach
        void setUp(){

            StorageSpot spot1 = StorageSpot.create(
                    StorageSpotName.from("Freezer"),
                    StorageSpotType.FREEZER
            );

            StorageSpot spot2 = StorageSpot.create(
                    StorageSpotName.from("Pantry"),
                    StorageSpotType.FREEZER
            );

            space1 = Space.reconstitute(
                    SpaceId.create(),
                    SpaceName.from("My House 1"),
                    Emoji.from("🏠"),
                    Set.of(spot1),
                    creatorUserId,
                    Set.of(creatorUserId)
            );

            space2 = Space.reconstitute(
                    SpaceId.create(),
                    SpaceName.from("My House 2"),
                    Emoji.from("🏠"),
                    Set.of(spot1, spot2),
                    creatorUserId,
                    Set.of(creatorUserId, participantUserId)
            );

            space3 = Space.reconstitute(
                    SpaceId.create(),
                    SpaceName.from("My House 3"),
                    Emoji.from("🏠"),
                    Set.of(spot2),
                    participantUserId,
                    Set.of(participantUserId)
            );

            space4 = Space.reconstitute(
                    SpaceId.create(),
                    SpaceName.from("My House 4"),
                    Emoji.from("🏠"),
                    Set.of(spot1, spot2),
                    participantUserId,
                    Set.of(creatorUserId, participantUserId)
            );

            adapter.save(space1);
            adapter.save(space2);
            adapter.save(space3);
            adapter.save(space4);

            spaceSpringDataRepository.flush();
        }
        @Test
        void getByParticipantId_ShouldReturnListOfSpacesTheUserIsParticipant(){

            List<Space> result1 = adapter.getByParticipantId(creatorUserId);

            assertEquals(3, result1.size());

            assertTrue(result1.contains(space1));
            assertTrue(result1.contains(space2));
            assertTrue(result1.contains(space4));
            assertFalse(result1.contains(space3));


            List<Space> result2 = adapter.getByParticipantId(participantUserId);

            assertEquals(3, result2.size());

            assertTrue(result2.contains(space2));
            assertTrue(result2.contains(space3));
            assertTrue(result2.contains(space4));
            assertFalse(result2.contains(space1));
        }

        @Test
        void getByParticipantId_ShouldReturnEmptyListWhenParticipantIdIsNotParticipant(){
            List<Space> result = adapter.getByParticipantId(UserId.create());
            assertTrue(result.isEmpty());
        }
    }

    private void insertAccount (UUID id, String email, String password_hash){
        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                id, email, password_hash
        );
    }

    private void insertUser(UUID id, UUID accountId, String email, String username){
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                id, accountId, email, username
        );
    }
}