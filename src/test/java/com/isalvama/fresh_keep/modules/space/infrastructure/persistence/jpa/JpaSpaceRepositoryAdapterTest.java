package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceParticipantMapper;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.StorageSpotsMapper;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaSpaceRepositoryAdapterTest.TestConfig.class, SpaceMapper.class, SpaceParticipantMapper.class,
        StorageSpotsMapper.class, JpaSpaceRepositoryAdapter.class})
class JpaSpaceRepositoryAdapterTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        Clock clock() {
            return Clock.systemUTC();
        }
    }

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

            assertEquals(2, saved.getParticipants().size());
            assertTrue(saved.getParticipants().stream()
                    .anyMatch(participant -> participant.getId().getParticipantId().equals(creatorUserId.value())));
            assertTrue(saved.getParticipants().stream()
                    .allMatch(participant -> participant.getJoinedAt() != null));

            assertNotNull(saved.getCreatedAt());
            assertTrue(saved.getCreatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
            assertTrue(saved.getCreatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
            assertNotNull(saved.getLastUpdatedAt());
            assertTrue(saved.getLastUpdatedAt().isAfter(Instant.now().minus(2, ChronoUnit.MINUTES)));
            assertTrue(saved.getLastUpdatedAt().isBefore(Instant.now().plus(2, ChronoUnit.MINUTES)));
        }
    }

    @Nested
    class FindStorageSpotsByIds {

        @Test
        void shouldReturnStorageSpotsMatchingTheRequestedIds() {
            UUID spaceId = UUID.randomUUID();
            UUID fridgeId = UUID.randomUUID();
            UUID pantryId = UUID.randomUUID();
            insertSpace(spaceId, "Kitchen", "🏠");
            insertStorageSpot(fridgeId, "Fridge", "FRIDGE", spaceId);
            insertStorageSpot(pantryId, "Pantry", "PANTRY", spaceId);

            List<StorageSpot> result = adapter.findStorageSpotsByIds(Set.of(
                    StorageSpotId.of(fridgeId), StorageSpotId.of(pantryId)));

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(spot -> spot.getId().value().equals(fridgeId)
                    && spot.getName().value().equals("Fridge")
                    && spot.getType() == StorageSpotType.FRIDGE));
            assertTrue(result.stream().anyMatch(spot -> spot.getId().value().equals(pantryId)
                    && spot.getName().value().equals("Pantry")
                    && spot.getType() == StorageSpotType.PANTRY));
        }

        @Test
        void shouldReturnOnlyExistingStorageSpots() {
            UUID spaceId = UUID.randomUUID();
            UUID existingId = UUID.randomUUID();
            insertSpace(spaceId, "Kitchen", "🏠");
            insertStorageSpot(existingId, "Fridge", "FRIDGE", spaceId);

            List<StorageSpot> result = adapter.findStorageSpotsByIds(Set.of(
                    StorageSpotId.of(existingId), StorageSpotId.create()));

            assertEquals(1, result.size());
            assertEquals(existingId, result.getFirst().getId().value());
        }
    }

    @Nested
    class GetSpaces {

        private Space space1;
        private Space space2;
        private Space space3;
        private Space space4;

        private SpaceId spaceId1;
        private SpaceId spaceId2;
        private SpaceId spaceId3;
        private SpaceId spaceId4;



        @BeforeEach
        void setUp(){

            spaceId1 = SpaceId.create();
            spaceId2 = SpaceId.create();
            spaceId3 = SpaceId.create();
            spaceId4 = SpaceId.create();

            StorageSpot spot1 = StorageSpot.create(
                    StorageSpotName.from("Freezer"),
                    StorageSpotType.FREEZER
            );

            StorageSpot spot2 = StorageSpot.create(
                    StorageSpotName.from("Pantry"),
                    StorageSpotType.FREEZER
            );

            space1 = Space.reconstitute(
                    spaceId1,
                    SpaceName.from("My House 1"),
                    Emoji.from("🏠"),
                    Set.of(spot1),
                    creatorUserId,
                    Set.of(creatorUserId)
            );

            space2 = Space.reconstitute(
                    spaceId2,
                    SpaceName.from("My House 2"),
                    Emoji.from("🏠"),
                    Set.of(spot1, spot2),
                    creatorUserId,
                    Set.of(creatorUserId, participantUserId)
            );

            space3 = Space.reconstitute(
                    spaceId3,
                    SpaceName.from("My House 3"),
                    Emoji.from("🏠"),
                    Set.of(spot2),
                    participantUserId,
                    Set.of(participantUserId)
            );

            space4 = Space.reconstitute(
                    spaceId4,
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

        @Test
        void getById_ShouldSpaceWithAMatchingId(){
            Optional<Space> result1 = adapter.getById(spaceId1);
            assertFalse(result1.isEmpty());
            assertEquals(result1.get(), space1);

            Optional<Space> result2 = adapter.getById(spaceId2);
            assertFalse(result2.isEmpty());
            assertEquals(result2.get(), space2);

            Optional<Space> result3 = adapter.getById(spaceId3);
            assertFalse(result3.isEmpty());
            assertEquals(result3.get(), space3);

            Optional<Space> result4 = adapter.getById(spaceId4);
            assertFalse(result4.isEmpty());
            assertEquals(result4.get(), space4);
        }

        @Test
        void getById_ShouldReturnEmptyListWhenASpaceWithAMatchingIdDoesNotExist(){
            Optional<Space> result = adapter.getById(SpaceId.create());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class ExistsByIdAndParticipantId {

        private StorageSpot spotOnlyCreatorCanAccess;
        private StorageSpot spotBothCanAccess;

        @BeforeEach
        void setUp() {
            spotOnlyCreatorCanAccess = StorageSpot.create(StorageSpotName.from("Freezer"), StorageSpotType.FREEZER);
            spotBothCanAccess = StorageSpot.create(StorageSpotName.from("Pantry"), StorageSpotType.PANTRY);

            Space creatorOnlySpace = Space.reconstitute(
                    SpaceId.create(), SpaceName.from("Creator Only"), Emoji.from("🏠"),
                    Set.of(spotOnlyCreatorCanAccess), creatorUserId, Set.of(creatorUserId)
            );
            Space sharedSpace = Space.reconstitute(
                    SpaceId.create(), SpaceName.from("Shared"), Emoji.from("🏠"),
                    Set.of(spotBothCanAccess), creatorUserId, Set.of(creatorUserId, participantUserId)
            );

            adapter.save(creatorOnlySpace);
            adapter.save(sharedSpace);
            spaceSpringDataRepository.flush();
        }

        @Test
        void shouldReturnTrueWhenUserIsParticipantOfSpaceOwningStorageSpot() {
            assertTrue(adapter.existsStorageSpotByIdAndParticipantId(participantUserId, spotBothCanAccess.getId()));
            assertTrue(adapter.existsStorageSpotByIdAndParticipantId(creatorUserId, spotOnlyCreatorCanAccess.getId()));
        }

        @Test
        void shouldReturnFalseWhenUserIsNotParticipantOfSpaceOwningStorageSpot() {
            assertFalse(adapter.existsStorageSpotByIdAndParticipantId(participantUserId, spotOnlyCreatorCanAccess.getId()));
        }

        @Test
        void shouldReturnFalseWhenStorageSpotDoesNotExist() {
            assertFalse(adapter.existsStorageSpotByIdAndParticipantId(creatorUserId, StorageSpotId.create()));
        }
    }

    @Nested
    class FindAccessible {

        private StorageSpot spotOnlyCreatorCanAccess;
        private StorageSpot spotBothCanAccess;

        @BeforeEach
        void setUp() {
            spotOnlyCreatorCanAccess = StorageSpot.create(StorageSpotName.from("Freezer"), StorageSpotType.FREEZER);
            spotBothCanAccess = StorageSpot.create(StorageSpotName.from("Pantry"), StorageSpotType.PANTRY);

            Space creatorOnlySpace = Space.reconstitute(
                    SpaceId.create(), SpaceName.from("Creator Only"), Emoji.from("🏠"),
                    Set.of(spotOnlyCreatorCanAccess), creatorUserId, Set.of(creatorUserId)
            );
            Space sharedSpace = Space.reconstitute(
                    SpaceId.create(), SpaceName.from("Shared"), Emoji.from("🏠"),
                    Set.of(spotBothCanAccess), creatorUserId, Set.of(creatorUserId, participantUserId)
            );

            adapter.save(creatorOnlySpace);
            adapter.save(sharedSpace);
            spaceSpringDataRepository.flush();
        }

        @Test
        void shouldReturnOnlyAccessibleStorageSpotIds() {
            Set<String> result = adapter.findAccessible(
                    participantUserId.value().toString(),
                    List.of(spotOnlyCreatorCanAccess.getId(), spotBothCanAccess.getId())
            );

            assertEquals(Set.of(spotBothCanAccess.getId().value().toString()), result);
        }

        @Test
        void shouldReturnEmptySetWhenNoneAccessible() {
            Set<String> result = adapter.findAccessible(
                    participantUserId.value().toString(),
                    List.of(spotOnlyCreatorCanAccess.getId())
            );

            assertTrue(result.isEmpty());
        }

        @Test
        void shouldIgnoreNonExistentStorageSpotIds() {
            Set<String> result = adapter.findAccessible(
                    creatorUserId.value().toString(),
                    List.of(spotOnlyCreatorCanAccess.getId(), StorageSpotId.create())
            );

            assertEquals(Set.of(spotOnlyCreatorCanAccess.getId().value().toString()), result);
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

    private void insertSpace(UUID id, String name, String emoji) {
        jdbcTemplate.update(
                "INSERT INTO spaces (id, name, emoji) VALUES (?, ?, ?)",
                id, name, emoji
        );
    }

    private void insertStorageSpot(UUID id, String name, String type, UUID spaceId) {
        jdbcTemplate.update(
                "INSERT INTO storage_spots (id, name, storage_spot_type, space_id) VALUES (?, ?, ?, ?)",
                id, name, type, spaceId
        );
    }
}
