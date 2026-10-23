package com.isalvama.fresh_keep.shared.infrastructure.ai;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageSpotsPromptFormatterTest {

    @Test
    void format_joinsMultipleStorageSpotsOnePerLine() {
        List<StorageSpotsPromptFormatter.StorageSpotData> storageSpots = List.of(
                new StorageSpotsPromptFormatter.StorageSpotData("fridge-id", "Fridge", "FRIDGE"),
                new StorageSpotsPromptFormatter.StorageSpotData("pantry-id", "Pantry", "PANTRY")
        );

        String result = StorageSpotsPromptFormatter.format(storageSpots);

        assertEquals("- id: fridge-id, name: Fridge, type: FRIDGE\n- id: pantry-id, name: Pantry, type: PANTRY", result);
    }

    @Test
    void format_formatsASingleStorageSpotWithoutTrailingNewline() {
        List<StorageSpotsPromptFormatter.StorageSpotData> storageSpots = List.of(
                new StorageSpotsPromptFormatter.StorageSpotData("fridge-id", "Fridge", "FRIDGE")
        );

        String result = StorageSpotsPromptFormatter.format(storageSpots);

        assertEquals("- id: fridge-id, name: Fridge, type: FRIDGE", result);
    }

    @Test
    void format_returnsEmptyStringForAnEmptyList() {
        String result = StorageSpotsPromptFormatter.format(List.of());

        assertEquals("", result);
    }

    @Test
    void formatWithTitle_putsTheTitleBeforeTheFormattedStorageSpot() {
        StorageSpotsPromptFormatter.StorageSpotData storageSpot =
                new StorageSpotsPromptFormatter.StorageSpotData("fridge-id", "Fridge", "FRIDGE");

        String result = StorageSpotsPromptFormatter.formatWithTitle(storageSpot, "Old Storage Spot");

        assertEquals("Old Storage Spot\n- id: fridge-id, name: Fridge, type: FRIDGE", result);
    }
}
