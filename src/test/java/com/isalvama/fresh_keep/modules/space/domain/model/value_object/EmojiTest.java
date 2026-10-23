package com.isalvama.fresh_keep.modules.space.domain.model.value_object;

import com.isalvama.fresh_keep.modules.space.domain.exception.InvalidEmojiException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmojiTest {

    @Test
    void from_doesNotThrowWhenValueIsSingleCodepointEmoji() {
        assertDoesNotThrow(() -> Emoji.from("😀")); // 😀 GRINNING FACE, 1 codepoint
    }

    @Test
    void from_doesNotThrowWhenValueIsTwoCodepointComposedEmoji() {
        assertDoesNotThrow(() -> Emoji.from("❤️")); // ❤️ HEAVY BLACK HEART + VARIATION SELECTOR-16, 2 codepoints
    }

    @Test
    void from_doesNotThrowWhenValueIsEmojiWithSkinToneModifier() {
        assertDoesNotThrow(() -> Emoji.from("👍🏽")); // 👍🏽 THUMBS UP + skin tone modifier, 2 codepoints
    }

    @Test
    void from_doesNotThrowWhenValueIsLegitimateMultiCodepointEmojiSequence() {

        String familyEmoji = "👨‍👩‍👦"; // 👨‍👩‍👦 FAMILY: MAN, WOMAN, BOY — 5 codepoints (MAN, ZWJ, WOMAN, ZWJ, BOY).
        assertDoesNotThrow(() -> Emoji.from(familyEmoji));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsNonEmojiText() {
        assertThrows(InvalidEmojiException.class, () -> Emoji.from("abc"));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsNonEmojiShortString() {
        assertThrows(InvalidEmojiException.class, () -> Emoji.from("ab"));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsNull() {
        assertThrows(InvalidEmojiException.class, () -> Emoji.from(null));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsEmpty() {
        assertThrows(InvalidEmojiException.class, () -> Emoji.from(""));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsBlank() {
        assertThrows(InvalidEmojiException.class, () -> Emoji.from(" "));
    }

    @Test
    void from_throwsInvalidEmojiExceptionWhenValueIsKeycapSequence() {
        String keycapOne = "1️⃣"; // 1️⃣ DIGIT ONE + VARIATION SELECTOR-16 + COMBINING ENCLOSING KEYCAP
        assertThrows(InvalidEmojiException.class, () -> Emoji.from(keycapOne));
    }

    @Test
    void value_returnsRawValue() {
        String grinningFace = "😀";
        assertEquals(grinningFace, Emoji.from(grinningFace).value());
    }
}
