package com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaSpaceEntity;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.entity.JpaStorageSpotEntity;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceMapperTest {

    @Mock
    private StorageSpotsMapper storageSpotsMapper;

    @InjectMocks
    private SpaceMapper spaceMapper;

    @Test
    void toDomain_mapsIdAndCreatorIdToTheirOwnFieldsRespectively() {
        UUID spaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();

        JpaStorageSpotEntity spotEntity = JpaStorageSpotEntity.builder()
                .id(UUID.randomUUID())
                .name("Main Shelf")
                .type(StorageSpotType.SHELF)
                .build();
        StorageSpot mappedSpot = StorageSpot.reconstitute(
                StorageSpotId.of(spotEntity.getId()), StorageSpotName.from("Main Shelf"), StorageSpotType.SHELF);
        when(storageSpotsMapper.toDomain(spotEntity)).thenReturn(mappedSpot);

        JpaSpaceEntity entity = JpaSpaceEntity.builder()
                .id(spaceId)
                .name("Kitchen")
                .emoji("🏠")
                .creatorId(creatorId)
                .storageSpots(Set.of(spotEntity))
                .participantIds(Set.of(creatorId))
                .build();

        Space domain = spaceMapper.toDomain(entity);

        assertEquals(spaceId, domain.getId().value());
        assertEquals(creatorId, domain.getCreatorId().value());
        assertNotEquals(domain.getId().value(), domain.getCreatorId().value());
    }

    @Test
    void toDomain_mapsAllFieldsCorrectly() {
        UUID spaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        UUID otherParticipantId = UUID.randomUUID();

        JpaStorageSpotEntity spotEntity = JpaStorageSpotEntity.builder()
                .id(UUID.randomUUID())
                .name("Main Shelf")
                .type(StorageSpotType.SHELF)
                .build();
        StorageSpot mappedSpot = StorageSpot.reconstitute(
                StorageSpotId.of(spotEntity.getId()), StorageSpotName.from("Main Shelf"), StorageSpotType.SHELF);
        when(storageSpotsMapper.toDomain(spotEntity)).thenReturn(mappedSpot);

        JpaSpaceEntity entity = JpaSpaceEntity.builder()
                .id(spaceId)
                .name("Kitchen")
                .emoji("🏠")
                .creatorId(creatorId)
                .storageSpots(Set.of(spotEntity))
                .participantIds(Set.of(creatorId, otherParticipantId))
                .build();

        Space domain = spaceMapper.toDomain(entity);

        assertEquals(spaceId, domain.getId().value());
        assertEquals("Kitchen", domain.getName().value());
        assertEquals("🏠", domain.getEmoji().value());
        assertEquals(creatorId, domain.getCreatorId().value());
        assertEquals(1, domain.getStorageSpots().size());
        assertTrue(domain.getStorageSpots().contains(mappedSpot));
        assertEquals(
                Set.of(creatorId, otherParticipantId),
                domain.getParticipantIds().stream().map(UserId::value).collect(Collectors.toSet())
        );
    }

    @Test
    void toEntity_mapsAllFieldsCorrectly() {
        UserId creatorId = UserId.create();
        StorageSpot spot = StorageSpot.create(StorageSpotName.from("Main Shelf"), StorageSpotType.SHELF);
        JpaStorageSpotEntity mappedSpotEntity = JpaStorageSpotEntity.builder()
                .id(spot.getId().value())
                .name("Main Shelf")
                .type(StorageSpotType.SHELF)
                .build();
        when(storageSpotsMapper.toEntity(spot)).thenReturn(mappedSpotEntity);

        Space space = Space.create(SpaceName.from("Kitchen"), Emoji.from("🏠"), Set.of(spot), creatorId);

        JpaSpaceEntity entity = spaceMapper.toEntity(space);

        assertEquals(space.getId().value(), entity.getId());
        assertEquals("Kitchen", entity.getName());
        assertEquals("🏠", entity.getEmoji());
        assertEquals(creatorId.value(), entity.getCreatorId());
        assertEquals(1, entity.getStorageSpots().size());
        assertTrue(entity.getStorageSpots().contains(mappedSpotEntity));
        assertSame(entity, mappedSpotEntity.getSpace());
        assertEquals(Set.of(creatorId.value()), entity.getParticipantIds());
    }
}
