package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReprocessShoppingReceiptWithFlaggedProductsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.ProductExtractionsPromptFormatter;
import com.isalvama.fresh_keep.shared.infrastructure.ai.StorageSpotsPromptFormatter;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PromptBuilder {

    public String build (String promptTextTemplate, ProcessNewShoppingReceiptDto dto, BeanOutputConverter<?> converter) {
        Map<String, Object> map = buildBaseMap(new CommonPlaceholders(dto.storageSpots(), dto.clock(), dto.productTypes(), dto.moneyCurrencies(), dto.language()), converter);
        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(map);
    }

    public String build (String promptTextTemplate, ReprocessShoppingReceiptWithFlaggedProductsDto dto, BeanOutputConverter<?> converter) {
        Map<String, Object> map = buildBaseMap(new CommonPlaceholders(dto.storageSpots(), dto.clock(), dto.productTypes(), dto.moneyCurrencies(), dto.language()), converter);

        map.put("purchaseDate", dto.purchaseDate().toString());
        map.put("storeName", dto.storeName());
        map.put("allProducts", ProductExtractionsPromptFormatter.format(dto.productExtractions()));
        map.put("productsToReview", ProductExtractionsPromptFormatter.format(dto.flaggedProductExtractions()));

        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(map);
    }

    private Map<String, Object> buildBaseMap(CommonPlaceholders cp, BeanOutputConverter<?> converter){

        List<StorageSpotsPromptFormatter.StorageSpotData> storageSpotData = cp.storageSpots().stream().map(sp -> new StorageSpotsPromptFormatter.StorageSpotData(
                sp.id(),
                sp.name(),
                sp.type()
        )).toList();

        return new HashMap<>(Map.of(
                "format", converter.getFormat(),
                "productTypes", String.join(", ", cp.productTypes()),
                "moneyCurrencies", String.join(", ", cp.moneyCurrencies()),
                "storageSpots", StorageSpotsPromptFormatter.format(storageSpotData),
                "today", LocalDateTime.now(cp.clock()).toString(),
                "language", cp.language
        ));
    }

    private record CommonPlaceholders(
            List<StorageSpotDto> storageSpots,
            Clock clock,
            List<String> productTypes,
            List<String> moneyCurrencies,
            String language
    ) {}
}
