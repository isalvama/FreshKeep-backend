package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.ProductChangesPromptFormatter;
import com.isalvama.fresh_keep.shared.infrastructure.ai.StorageSpotsPromptFormatter;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReviewPromptBuilder {

    public String build(String promptTextTemplate, ReviewNewShoppingReceiptDto dto, BeanOutputConverter<ReceiptExtractionToReview> converter) {

        List<StorageSpotsPromptFormatter.StorageSpotData> storageSpotData = dto.storageSpots().stream().map(sp -> new StorageSpotsPromptFormatter.StorageSpotData(
                sp.id(),
                sp.name(),
                sp.type()
                )
        ).toList();

        String storageSpotsList = StorageSpotsPromptFormatter.format(storageSpotData);

        Map<String, Object> map = buildBaseMap(new CommonPlaceholders(dto.shoppingDate()), converter);
        map.put("storageSpots", storageSpotsList);
        map.put("products", dto.productExtractions().toString());

        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(map);
    }


    public String build(String promptTextTemplate, ProductMovedDto dto, BeanOutputConverter<LocalDate> converter) {

        String productChangesList = ProductChangesPromptFormatter.format(dto.productChanges());

        String oldStorageSpotString = StorageSpotsPromptFormatter.formatWithTitle(new StorageSpotsPromptFormatter.StorageSpotData(
                        dto.oldStorageSpotInfo().id(),
                        dto.oldStorageSpotInfo().name(),
                        dto.oldStorageSpotInfo().type()
                ),
                "Old Storage Spot"
        );

        String newStorageSpotString = StorageSpotsPromptFormatter.formatWithTitle(new StorageSpotsPromptFormatter.StorageSpotData(
                        dto.newStorageSpotInfo().id(),
                        dto.newStorageSpotInfo().name(),
                        dto.newStorageSpotInfo().type()
                ),
                "New Storage Spot"
        );

        Map<String, Object> map = buildBaseMap(new CommonPlaceholders(dto.shoppingDate()), converter);
        map.put("productName", dto.productName());
        map.put("productType", dto.productType());
        map.put("productChanges", productChangesList);
        map.put("oldStorageSpot", oldStorageSpotString);
        map.put("newStorageSpot", newStorageSpotString);
        map.put("today", LocalDateTime.now(dto.clock()).toString());

        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(map);
    }

    private Map<String, Object> buildBaseMap(CommonPlaceholders cp, BeanOutputConverter<?> converter){
        return new HashMap<>(Map.of(
                "format", converter.getFormat(),
                "shoppingDate", cp.shoppingDate()
        ));
    }

    private record CommonPlaceholders(
            LocalDate shoppingDate
    ) {}
}
