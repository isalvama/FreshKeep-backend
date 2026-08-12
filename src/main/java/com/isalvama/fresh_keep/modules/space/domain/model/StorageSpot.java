package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorageSpot {
    private final StorageSpotId id;
    private StorageSpotName name;
    private final StorageSpotType type;

    private StorageSpot(StorageSpotId id, StorageSpotName name, StorageSpotType type) {
        this.id = validateNotNull(id, "id");
        this.name = validateNotNull(name, "name");
        this.type = validateNotNull(type, "type");
    }

    public static StorageSpot create (StorageSpotName name, StorageSpotType type){
        return new StorageSpot(
                StorageSpotId.create(),
                name,
                type
        );
    }

    public static StorageSpot reconstitute (StorageSpotId id, StorageSpotName name, StorageSpotType type){
        return new StorageSpot(
                id,
                name,
                type
        );
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidSpaceException(fieldName + " cannot be null.");
        return fieldValue;
    }
}
