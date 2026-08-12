package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StorageSpotTest {
    private static final String NAME = "Storage Spot Name";


    @Test
    void create_generatesStorageSpotWithGeneratedId(){
        StorageSpot storageSpot = StorageSpot.create(StorageSpotName.from(NAME), StorageSpotType.FREEZER);
        assertNotNull(storageSpot);
        assertNotNull(storageSpot.getId());
        assertEquals(NAME, storageSpot.getName().value());
        assertEquals(StorageSpotType.FREEZER, storageSpot.getType());
    }

    @Test
    void create_throwsInvalidSpaceExceptionWhenParamsAreNull(){
        assertThrows(InvalidSpaceException.class, () -> {StorageSpot.create(null, StorageSpotType.FREEZER);});
        assertThrows(InvalidSpaceException.class, () -> {StorageSpot.create(StorageSpotName.from(NAME), null);});
    }

    @Test
    void reconstitute_generatesStorageSpotSuccessfully(){
        StorageSpotId id = StorageSpotId.create();
        StorageSpot storageSpot = StorageSpot.reconstitute(id, StorageSpotName.from(NAME), StorageSpotType.FREEZER);
        assertNotNull(storageSpot);
        assertEquals(id.toString(), storageSpot.getId().toString());
        assertEquals(NAME, storageSpot.getName().value());
        assertEquals(StorageSpotType.FREEZER, storageSpot.getType());
    }

    @Test
    void reconstitute_throwsInvalidSpaceExceptionWhenParamsAreNull(){
        assertThrows(InvalidSpaceException.class, () -> {StorageSpot.reconstitute(null, StorageSpotName.from(NAME), StorageSpotType.FREEZER);});
        assertThrows(InvalidSpaceException.class, () -> {StorageSpot.reconstitute(StorageSpotId.create(), null, StorageSpotType.FREEZER);});
        assertThrows(InvalidSpaceException.class, () -> {StorageSpot.reconstitute(StorageSpotId.create(), StorageSpotName.from(NAME), null);});
    }

}