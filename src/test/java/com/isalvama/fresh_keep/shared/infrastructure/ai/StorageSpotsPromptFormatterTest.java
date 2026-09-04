package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StorageSpotsPromptFormatterTest {

    @Test
    void format_joinsMultipleStorageSpotsOnePerLine() {
        List<StorageSpotDto> storageSpots = List.of(
                StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"),
                StorageSpotDto.create("pantry-id", "Pantry", "PANTRY")
        );

        String result = StorageSpotsPromptFormatter.format(storageSpots);

        assertEquals("- id: fridge-id, name: Fridge, type: FRIDGE\n- id: pantry-id, name: Pantry, type: PANTRY", result);
    }

    @Test
    void format_formatsASingleStorageSpotWithoutTrailingNewline() {
        List<StorageSpotDto> storageSpots = List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"));

        String result = StorageSpotsPromptFormatter.format(storageSpots);

        assertEquals("- id: fridge-id, name: Fridge, type: FRIDGE", result);
    }

    @Test
    void format_returnsEmptyStringForAnEmptyList() {
        String result = StorageSpotsPromptFormatter.format(List.of());

        assertEquals("", result);
    }
}
