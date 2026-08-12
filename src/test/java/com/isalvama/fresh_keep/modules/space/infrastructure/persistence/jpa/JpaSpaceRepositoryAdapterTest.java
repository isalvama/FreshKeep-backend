package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa;

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
    private JpaSpaceRepositoryAdapter adapter;

    @Autowired
    private SpaceSpringDataRepository spaceSpringDataRepository;

    @Test
    void save_shouldPersistSpaceAndItsRelationsInRealPostgres() {
        SpaceId spaceId = SpaceId.create();
        UserId creatorId = UserId.create();

        StorageSpot spot = StorageSpot.create(
                StorageSpotName.from("Main Freezer"),
                StorageSpotType.FREEZER
        );

        Space space = Space.reconstitute(
                spaceId,
                SpaceName.from("My House"),
                Set.of(spot),
                creatorId,
                Set.of(creatorId, UserId.create())
        );

        adapter.save(space);


        JpaSpaceEntity saved = spaceSpringDataRepository.findById(spaceId.value()).orElseThrow();

        assertEquals(spaceId.value(), saved.getId());
        assertEquals("My House", saved.getName());
        assertEquals(creatorId.value(), saved.getCreatorId());

        assertEquals(1, saved.getStorageSpots().size());
        assertEquals("Main Freezer", saved.getStorageSpots().iterator().next().getName());
        assertEquals(StorageSpotType.FREEZER, saved.getStorageSpots().iterator().next().getType());

        assertEquals(2, saved.getParticipantIds().size());
        assertTrue(saved.getParticipantIds().contains(creatorId.value()));
    }
}