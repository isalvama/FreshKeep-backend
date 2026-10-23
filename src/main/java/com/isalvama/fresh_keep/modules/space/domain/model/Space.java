package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@EqualsAndHashCode(of = "id")
public class Space {
    private final SpaceId id;
    private SpaceName name;
    private Emoji emoji;
    private Set<StorageSpot> storageSpots;
    private final UserId creatorId;
    private Set<UserId> participantIds;

    private Space(SpaceId spaceId, SpaceName spaceName, Emoji emoji, Set<StorageSpot> storageSpots, UserId creatorId, Set<UserId> participantIds) {
        this.id = validateNotNull(spaceId, "spaceId");
        this.name = validateNotNull(spaceName, "spaceName");
        this.emoji = validateNotNull(emoji, "emoji");
        this.storageSpots = validateNotNullNotEmptyNotDuplicateStorageSpots(storageSpots);
        this.creatorId = validateNotNull(creatorId, "creatorId");
        this.participantIds = validateNotNullAndNotEmpty(participantIds, "participantIds");
    }

    public static Space create(SpaceName name, Emoji emoji, Set<StorageSpot> storageSpots, UserId creatorId){
        return new Space(
                SpaceId.create(),
                name,
                emoji,
                storageSpots,
                creatorId,
                Set.of(creatorId)
        );
    }

    public static Space reconstitute(SpaceId id, SpaceName name, Emoji emoji, Set<StorageSpot> storageSpots, UserId creatorId, Set<UserId> participantIds){
        return new Space(
                id,
                name,
                emoji,
                storageSpots,
                creatorId,
                participantIds
        );
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

    private static Set<StorageSpot> validateNotNullNotEmptyNotDuplicateStorageSpots(Set<StorageSpot> storageSpots) {
        validateNotNullAndNotEmpty(storageSpots, "storageSpots");

        Set<String> seen = new HashSet<>();

        List<StorageSpot> duplicates = storageSpots.stream()
                .filter(spot -> !seen.add(spot.getName().toString() + spot.getType()))
                .toList();

        if (!duplicates.isEmpty()){
            throw new InvalidSpaceException(String.format("Two or more storage spots of the same space cannot share the same name and type. %s storage spots are duplicates", duplicates.stream().map(d -> d.getName().value()).collect(Collectors.joining(", "))));
        }
        return storageSpots;
    }

}
