package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PromptBuilder {

    public String build (String promptTextTemplate, ProcessNewShoppingReceiptDto processNewShoppingReceiptDto, BeanOutputConverter<?> converter){
        String storageSpotsList = processNewShoppingReceiptDto.storageSpots().stream()
                .map(s -> "- id: %s, name: %s, type: %s".formatted(s.id(), s.name(), s.type()))
                .collect(Collectors.joining("\n"));

        String productTypes = String.join(", ", processNewShoppingReceiptDto.productTypes());
        String moneyCurrencies = String.join(", ", processNewShoppingReceiptDto.moneyCurrencies());
        String today = LocalDateTime.now(processNewShoppingReceiptDto.clock()).toString();


        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        String finalInstructions = promptTemplate.render(Map.of(
                "format", converter.getFormat(),
                "productTypes", productTypes,
                "moneyCurrencies", moneyCurrencies,
                "storageSpots", storageSpotsList,
                "today", today
        ));

        return finalInstructions;
    }
}
