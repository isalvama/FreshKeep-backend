package com.isalvama.fresh_keep.modules.space.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidEmojiException extends DomainException {
    public InvalidEmojiException(String message) {
        super("Invalid Emoji: " + message);
    }
}
