package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidEmojiException;

import java.util.regex.Pattern;

public record Emoji(String value) {

    private static final Pattern EMOJI_PATTERN = Pattern.compile(
            "^[[\\p{IsEmoji}&&[^0-9#*]]\\p{IsEmoji_Modifier}\\u200d\\uFE0F]+$"
    );

    public Emoji {
        if (value == null) {
            throw new InvalidEmojiException("The emoji value cannot be null");
        }
        if (value.isBlank()) {
            throw new InvalidEmojiException("The emoji value cannot be blank");
        }
        if (!isValidEmoji(value)) {
            throw new InvalidEmojiException("The string '" + value + "' is not a valid emoji.");
        }
    }

    private boolean isValidEmoji(String s) {
        return EMOJI_PATTERN.matcher(s).matches();
    }

    public static Emoji from (String value) {
        return new Emoji(value);
    }
}
