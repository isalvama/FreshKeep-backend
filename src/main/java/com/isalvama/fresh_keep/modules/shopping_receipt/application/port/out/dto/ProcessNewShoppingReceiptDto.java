package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto;

import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.util.List;

public record ProcessNewShoppingReceiptDto(
        MultipartFile file,
        List<StorageSpotDto> storageSpots,
        Clock clock,
        List<String> productTypes,
        List<String> moneyCurrencies
) {
}
