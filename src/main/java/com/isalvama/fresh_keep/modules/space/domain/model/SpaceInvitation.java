package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceInvitationException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Count;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceInvitationId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;

import java.time.Clock;
import java.time.LocalDateTime;

@Getter
public class SpaceInvitation {
    private static final int LIFE_SPAN_HOURS = 24;

    private final SpaceInvitationId id;
    private final Token token;
    private final SpaceId spaceId;
    private final UserId userCreatorId;
    private LocalDateTime expiresAt;
    private Boolean isActive;
    private Count maxUses;
    private Count usesCount;


    public SpaceInvitation(SpaceInvitationId id, Token token, SpaceId spaceId, UserId userCreatorId, LocalDateTime expiresAt, Boolean isActive, Count usesCount) {
        this.id = validateNotNull(id, "id");
        this.token = validateNotNull(token, "token");
        this.spaceId = validateNotNull(spaceId, "spaceId");
        this.userCreatorId = validateNotNull(userCreatorId, "userCreatorId");
        this.expiresAt = validateNotNull(expiresAt, "expiresAt");
        this.isActive = validateNotNull(isActive, "isActive");
        this.usesCount = validateNotNull(usesCount, "usesCount");
    }

    public static SpaceInvitation createWithoutMaxCount (SpaceId spaceId, UserId userCreatorId, Clock clock){
        return new SpaceInvitation(
                SpaceInvitationId.create(),
                Token.create(),
                spaceId,
                userCreatorId,
                LocalDateTime.now(clock).plusHours(LIFE_SPAN_HOURS),
                true,
                Count.of(0)
        );
    }

    public static SpaceInvitation reconstitute (SpaceInvitationId id, Token token, SpaceId spaceId, UserId userCreatorId, LocalDateTime expiresAt, Boolean isActive, Count maxUses, Count usesCount){
        if (maxUses != null && usesCount.value() > maxUses.value()){
            throw new InvalidSpaceInvitationException("maxUses cannot be greater than usesCount");
        }
        SpaceInvitation invitation = new SpaceInvitation(
                id,
                token,
                spaceId,
                userCreatorId,
                expiresAt,
                isActive,
                usesCount
        );
        invitation.setMaxUses(maxUses);
        return invitation;
    }

    private void setMaxUses(Count maxUses){
        this.maxUses = maxUses;
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidSpaceException(fieldName + " cannot be null.");
        return fieldValue;
    }
}
