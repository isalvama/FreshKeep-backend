package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;

import java.time.Clock;
import java.util.List;

public record RectifyExtractionDto(
        ReceiptExtraction extraction,
        List<StorageSpotDto> storageSpots,
        Clock clock
) {}