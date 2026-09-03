package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.StorageSpotsPromptFormatter;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PromptBuilder {

    public String build (String promptTextTemplate, ProcessNewShoppingReceiptDto processNewShoppingReceiptDto, BeanOutputConverter<?> converter) {
        String storageSpotsList = StorageSpotsPromptFormatter.format(processNewShoppingReceiptDto.storageSpots());

        String productTypes = String.join(", ", processNewShoppingReceiptDto.productTypes());
        String moneyCurrencies = String.join(", ", processNewShoppingReceiptDto.moneyCurrencies());
        String today = LocalDateTime.now(processNewShoppingReceiptDto.clock()).toString();


        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(Map.of(
                        "format", converter.getFormat(),
                        "productTypes", productTypes,
                        "moneyCurrencies", moneyCurrencies,
                        "storageSpots", storageSpotsList,
                        "today", today
                ));
    }
}
