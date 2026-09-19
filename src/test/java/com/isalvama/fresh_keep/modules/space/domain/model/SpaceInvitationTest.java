package com.isalvama.fresh_keep.modules.space.domain.model;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceException;
import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceInvitationException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Count;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceInvitationId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Token;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SpaceInvitationTest {

    private static final SpaceInvitationId ID = SpaceInvitationId.create();
    private static final Token TOKEN = Token.create();
    private static final SpaceId SPACE_ID = SpaceId.create();
    private static final UserId CREATOR_ID = UserId.create();
    private static final LocalDateTime EXPIRES_AT = LocalDateTime.now().plusDays(7);

    @Test
    void constructor_createsInvitationWhenRequiredValuesAreValid() {
        assertDoesNotThrow(() -> new SpaceInvitation(
                ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(0)));
    }

    @Test
    void constructor_throwsWhenARequiredValueIsNull() {
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(null, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, null, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, TOKEN, null, CREATOR_ID, EXPIRES_AT, true, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, TOKEN, SPACE_ID, null, EXPIRES_AT, true, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, TOKEN, SPACE_ID, CREATOR_ID, null, true, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, null, Count.of(0)));
        assertThrows(InvalidSpaceException.class,
                () -> new SpaceInvitation(ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, null));
    }

    @Test
    void createWithoutMaxCount_createsActiveInvitationWithNoUsageLimit() {
        SpaceInvitation invitation = validInvitation();

        SpaceInvitation created = invitation.createWithoutMaxCount(SPACE_ID, CREATOR_ID, EXPIRES_AT);

        assertNotNull(created.getId());
        assertNotNull(created.getToken());
        assertEquals(SPACE_ID, created.getSpaceId());
        assertEquals(CREATOR_ID, created.getUserCreatorId());
        assertEquals(EXPIRES_AT, created.getExpiresAt());
        assertTrue(created.getIsActive());
        assertNull(created.getMaxUses());
        assertEquals(0, created.getUsesCount().value());
    }

    @Test
    void reconstitute_createsInvitationWithNoUsageLimit() {
        SpaceInvitation reconstituted = validInvitation().reconstitute(
                ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, null, Count.of(0));

        assertEquals(ID, reconstituted.getId());
        assertEquals(TOKEN, reconstituted.getToken());
        assertEquals(EXPIRES_AT, reconstituted.getExpiresAt());
        assertNull(reconstituted.getMaxUses());
    }

    @Test
    void reconstitute_acceptsUsageCountEqualToMaximum() {
        assertDoesNotThrow(() -> validInvitation().reconstitute(
                ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(2), Count.of(2)));
    }

    @Test
    void reconstitute_throwsWhenUsageCountExceedsMaximum() {
        assertThrows(InvalidSpaceInvitationException.class, () -> validInvitation().reconstitute(
                ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(1), Count.of(2)));
    }

    private SpaceInvitation validInvitation() {
        return new SpaceInvitation(ID, TOKEN, SPACE_ID, CREATOR_ID, EXPIRES_AT, true, Count.of(0));
    }
}
