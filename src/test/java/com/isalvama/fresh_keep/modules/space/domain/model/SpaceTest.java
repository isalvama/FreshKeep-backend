package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SpaceTest {
    private static final String NAME = "Space Name";
    private static final SpaceId SPACE_ID = SpaceId.create();
    private static final UserId CREATOR_ID = UserId.create();


    @Test
    void create_generatesSpaceWithGeneratedIdAndCreatorIdAsParticipant(){
        StorageSpot storageSpot = StorageSpot.create(StorageSpotName.from("Storage Spot"), StorageSpotType.FREEZER);
        UserId creatorId = UserId.create();
        Space space = Space.create(SpaceName.from(NAME), Set.of(storageSpot), creatorId);

        assertNotNull(space);
        assertNotNull(space.getId());
        assertEquals(NAME, space.getName().value());
        assertEquals(1, space.getStorageSpots().size());
        assertTrue(space.getStorageSpots().contains(storageSpot));
        assertEquals(creatorId.value().toString(), space.getCreatorId().toString());
        assertEquals(1, space.getParticipantIds().size());
        assertTrue(space.getParticipantIds().contains(creatorId));
    }

    @Test
    void create_throwsException_whenSpotsHaveSameNameAndType() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Drawer"), StorageSpotType.FREEZER);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Drawer"), StorageSpotType.FREEZER);

        Set<StorageSpot> spots = Set.of(spot1, spot2);

        InvalidSpaceException ex = assertThrows(InvalidSpaceException.class,
                () -> Space.create(SpaceName.from(NAME), spots, CREATOR_ID));

        assertTrue(ex.getMessage().contains("cannot share the same name and type"));
    }

    @Test
    void create_allowsSpotsWithSameNameButDifferentType() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Main"), StorageSpotType.FREEZER);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Main"), StorageSpotType.FRIDGE);

        assertDoesNotThrow(() -> Space.create(SpaceName.from(NAME), Set.of(spot1, spot2), CREATOR_ID));
    }

    @Test
    void create_allowsSpotsWithSameTypeButDifferentName() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Fridge 1"), StorageSpotType.FRIDGE);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Fridge 2"), StorageSpotType.FRIDGE);

        assertDoesNotThrow(() -> Space.create(SpaceName.from(NAME), Set.of(spot1, spot2), CREATOR_ID));
    }

    @Test
    void create_throwsException_whenNameIsNull() {
        Set<StorageSpot> spots = Set.of(StorageSpot.create(StorageSpotName.from("Spot"), StorageSpotType.FRIDGE));

        assertThrows(InvalidSpaceException.class,
                () -> Space.create(null, spots, CREATOR_ID));
    }

    @Test
    void create_throwsException_whenStorageSpotsIsEmpty() {
        Set<StorageSpot> emptySpots = Set.of();

        InvalidSpaceException ex = assertThrows(InvalidSpaceException.class,
                () -> Space.create(SpaceName.from(NAME), emptySpots, CREATOR_ID));

        assertTrue(ex.getMessage().contains("storageSpots cannot be empty"));
    }

    @Test
    void reconstitute_createsSpaceWithExistingData() {
        SpaceId existingId = SpaceId.create();
        Set<StorageSpot> spots = Set.of(StorageSpot.create(StorageSpotName.from("Spot"), StorageSpotType.FREEZER));
        Set<UserId> participants = Set.of(CREATOR_ID, UserId.create());

        Space space = Space.reconstitute(existingId, SpaceName.from(NAME), spots, CREATOR_ID, participants);

        assertEquals(existingId, space.getId());
        assertEquals(2, space.getParticipantIds().size());
        assertTrue(space.getParticipantIds().contains(CREATOR_ID));
    }

    @Test
    void reconstitute_throwsException_whenSpotsHaveSameNameAndType() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Drawer"), StorageSpotType.FREEZER);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Drawer"), StorageSpotType.FREEZER);

        Set<StorageSpot> spots = Set.of(spot1, spot2);

        InvalidSpaceException ex = assertThrows(InvalidSpaceException.class,
                () -> Space.reconstitute(SPACE_ID, SpaceName.from(NAME), spots, CREATOR_ID, Set.of(CREATOR_ID)));

        assertTrue(ex.getMessage().contains("cannot share the same name and type"));
    }

    @Test
    void reconstitute_allowsSpotsWithSameNameButDifferentType() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Main"), StorageSpotType.FREEZER);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Main"), StorageSpotType.FRIDGE);

        assertDoesNotThrow(() -> Space.reconstitute(SPACE_ID, SpaceName.from(NAME), Set.of(spot1, spot2), CREATOR_ID, Set.of(CREATOR_ID)));
    }

    @Test
    void reconstitute_allowsSpotsWithSameTypeButDifferentName() {
        StorageSpot spot1 = StorageSpot.create(StorageSpotName.from("Fridge 1"), StorageSpotType.FRIDGE);
        StorageSpot spot2 = StorageSpot.create(StorageSpotName.from("Fridge 2"), StorageSpotType.FRIDGE);

        assertDoesNotThrow(() -> Space.reconstitute(SPACE_ID, SpaceName.from(NAME), Set.of(spot1, spot2), CREATOR_ID, Set.of(CREATOR_ID)));
    }

    @Test
    void reconstitute_throwsException_whenParticipantsIsEmpty() {
        Set<StorageSpot> spots = Set.of(StorageSpot.create(StorageSpotName.from("Spot"), StorageSpotType.FRIDGE));
        Set<UserId> emptyParticipants = Set.of();

        InvalidSpaceException ex = assertThrows(InvalidSpaceException.class,
                () -> Space.reconstitute(SPACE_ID, SpaceName.from(NAME), spots, CREATOR_ID, emptyParticipants));

        assertTrue(ex.getMessage().contains("participantIds cannot be empty"));
    }
}