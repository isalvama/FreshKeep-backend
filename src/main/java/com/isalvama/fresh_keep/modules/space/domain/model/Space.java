package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Set;

@Setter
@Getter
public class Space {
    private final SpaceId id;
    private SpaceName name;
    private Set<StorageSpot> storageSpots;
    private final UserId creatorId;
    private Set<UserId> participantIds;
    private Set<ShoppingReceiptId> shoppingReceiptIds;

    private Space(SpaceId spaceId, SpaceName spaceName, Set<StorageSpot> storageSpots, UserId creatorId) {
        this.id = validateNotNull(spaceId, "spaceId");
        this.name = validateNotNull(spaceName, "spaceName");
        this.storageSpots = validateNotNullAndNotEmpty(storageSpots, "storageSpots");
        this.creatorId = creatorId;
    }

    public Space create(SpaceName name, Set<StorageSpot> storageSpots, UserId creatorId){
        Space space = new Space(
                SpaceId.create(),
                name,
                storageSpots,
                creatorId
        );
        space.setParticipantIds(Set.of(creatorId));
        space.setShoppingReceiptIds(Set.of());
        return space;
    }

    public Space reconstitute(SpaceId id, SpaceName name, Set<StorageSpot> storageSpots, UserId creatorId, Set<UserId> participantIds, Set<ShoppingReceiptId> shoppingReceiptIds){
        Space location = new Space(
                id,
                name,
                storageSpots,
                creatorId
        );
        location.setParticipantIds(validateNotNull(participantIds, "participantIds"));
        location.setShoppingReceiptIds(validateNotNull(shoppingReceiptIds, "shoppingReceiptIds"));
        return location;
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidSpaceException(fieldName + " cannot be null.");
        return fieldValue;
    }

    private static <T extends Collection<?>> T validateNotNullAndNotEmpty(T fieldValue, String fieldName) {
        validateNotNull(fieldValue, fieldName);

        if (fieldValue.isEmpty())
            throw new InvalidSpaceException(fieldName + " cannot be empty.");
        return fieldValue;
    }


}
