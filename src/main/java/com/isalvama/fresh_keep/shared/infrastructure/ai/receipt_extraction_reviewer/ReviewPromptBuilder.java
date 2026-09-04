package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.StorageSpotsPromptFormatter;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ReviewPromptBuilder {

    public String build(String promptTextTemplate, ReviewNewShoppingReceiptDto dto, BeanOutputConverter<ReceiptExtractionToReview> converter) {
        String storageSpotsList = StorageSpotsPromptFormatter.format(dto.storageSpots());

        PromptTemplate promptTemplate = new PromptTemplate(promptTextTemplate);
        return promptTemplate.render(Map.of(
                "format", converter.getFormat(),
                "shoppingDate", dto.shoppingDate().toString(),
                "storageSpots", storageSpotsList,
                "products", dto.productExtractions().toString()
        ));
    }
}
