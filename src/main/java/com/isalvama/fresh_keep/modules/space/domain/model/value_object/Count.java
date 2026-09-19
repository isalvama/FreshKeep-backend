package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidInvitationCountException;

public record Count(Integer value) {

    public Count{
        if (value == null){
            throw new InvalidInvitationCountException("Invitation count cannot be null");
        }

        if (value < 0){
            throw new InvalidInvitationCountException("Invitation count cannot be negative");
        }
    }

    public static Count of (Integer value){
        return new Count(value);
    }
}
