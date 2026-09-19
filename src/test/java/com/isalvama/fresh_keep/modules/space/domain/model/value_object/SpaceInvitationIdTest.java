package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidSpaceInvitationIdException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpaceInvitationIdTest {

    @Test
    void create_generatesNonNullRandomUuid() {
        SpaceInvitationId first = SpaceInvitationId.create();
        SpaceInvitationId second = SpaceInvitationId.create();

        assertNotNull(first.value());
        assertNotEquals(first, second);
    }

    @Test
    void of_uuid_returnsIdWithSameValue() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, SpaceInvitationId.of(uuid).value());
    }

    @Test
    void of_uuid_throwsWhenValueIsNull() {
        assertThrows(InvalidSpaceInvitationIdException.class,
                () -> SpaceInvitationId.of((UUID) null));
    }

    @Test
    void from_string_returnsIdWithParsedValue() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid, SpaceInvitationId.from(uuid.toString()).value());
    }

    @Test
    void from_string_throwsWhenValueIsNullBlankOrInvalid() {
        assertThrows(InvalidSpaceInvitationIdException.class,
                () -> SpaceInvitationId.from((String) null));
        assertThrows(InvalidSpaceInvitationIdException.class,
                () -> SpaceInvitationId.from(" "));
        assertThrows(InvalidSpaceInvitationIdException.class,
                () -> SpaceInvitationId.from("not-a-uuid"));
    }

    @Test
    void toString_returnsUuidStringRepresentation() {
        UUID uuid = UUID.randomUUID();

        assertEquals(uuid.toString(), SpaceInvitationId.of(uuid).toString());
    }
}
