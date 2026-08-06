package com.isalvama.fresh_keep.modules.space.domain.model;

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
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public static StorageSpot create (StorageSpotName name, StorageSpotType type){
        return new StorageSpot(
                StorageSpotId.create(),
                name,
                type
        );
    }
}
