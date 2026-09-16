package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.AiReceiptExtractionReviewerPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaReceiptExtractionReviewerAdapter implements AiReceiptExtractionReviewerPort {
    private final OllamaChatModel chatModel;
    private final ReviewPromptBuilder reviewPromptBuilder;

    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;

    private static final String TEMPLATE_PROMPT_TEXT = """
            You are reviewing a list of grocery products that were just extracted from a shopping receipt by another AI system. Your job is to catch mistakes, not to redo the extraction.

            For each product below, decide whether a human should double-check it because something about its data looks implausible. Only flag a product when you are reasonably confident something is wrong. Expiration dates are estimates by nature, so do not flag a product just because you would have guessed a slightly different number of days - only flag it if it is clearly, obviously wrong.

            Check each product against these rules:
            1. Name: does "name" look like a real, recognizable food or grocery item? Flag it if it is garbled, meaningless, or clearly not a food product (e.g. leftover OCR noise like "ART 4471" or a non-food item).
            2. Expiration date: is "expirationDate" a plausible shelf life for this kind of product, counting from {shoppingDate} and taking into account its storage spot's type? Flag it if it is clearly wrong for the product type - e.g. a fresh product expiring years from now, a frozen or shelf-stable product expiring in a day or two, or a date already in the past.
            3. Storage spot: does the storage spot suggested for the product make sense, given that spot's type? Flag it if the pairing is clearly wrong - e.g. raw meat, seafood or dairy suggested for a PANTRY or WINE_CELLAR spot instead of a FRIDGE/FREEZER one, or a frozen product suggested for anything other than a FREEZER spot. If a product's suggestedStorageSpotId is empty, do not flag it for that reason alone. The user's available storage spots are:
            {storageSpots}
            4. Product type: does the product type suggested for the product make sense, given the type of product? Flag it if the pairing is clearly wrong.
            5. Price Amount: flag the product if the price is negative or clearly overpriced or too inexpensive.

            Here are the extracted products, each identified by its productIndex:
            {products}

            Return only the products that need review, referencing each one by its productIndex, together with a short, one-sentence reason written for the end user explaining what looks wrong with it. Do not return anything for products that look fine.

            {format}
            """;

    @Override
    @Retryable(retryFor = AiRetryableException.class, maxAttempts = 2,  backoff = @Backoff(delay = 1000))
    public List<ProductReviewFlag> review(ReviewNewShoppingReceiptDto dto) {

        if (dto == null || dto.productExtractions() == null || dto.productExtractions().isEmpty() || dto.shoppingDate() == null){
            log.error("OllamaReceiptExtractionReviewerAdapter.review() returns an empty list because ReviewNewShoppingReceiptDto instance is null or has null shoppingDate or null or empty productExtractions");
            return List.of();
        }

        BeanOutputConverter<ReceiptExtractionToReview> converter = new BeanOutputConverter<>(ReceiptExtractionToReview.class);

        OllamaChatOptions chatOptions = OllamaChatOptions.builder()
                .model(model)
                .outputSchema(converter.getJsonSchema())
                .build();
        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(reviewPromptBuilder.build(TEMPLATE_PROMPT_TEXT, dto, converter), chatOptions));
        } catch (RuntimeException e) {
            throw new AiRetryableException("Unable to reach the AI reviewer.", e);
        }

        return parseAndValidate(response, converter);
    }

    private List<ProductReviewFlag> parseAndValidate(ChatResponse response, BeanOutputConverter<ReceiptExtractionToReview> converter) {

        if (response.getResult() == null) {
            throw new AiRetryableException("The AI reviewer did not return any response to the ticket");
        }

        String jsonText = response.getResult().getOutput().getText();

        if (jsonText == null || jsonText.isBlank() || !jsonText.trim().startsWith("{")) {
            throw new AiRetryableException("The AI reviewer returned and empty response.");
        }

        ReceiptExtractionToReview receiptExtractionToReview;

        try {
             receiptExtractionToReview = converter.convert(jsonText);
        } catch (Exception e) {
            throw new AiRetryableException("The AI reviewer answer could not be parsed", e);
        }

        if (receiptExtractionToReview.flaggedProducts() == null || receiptExtractionToReview.flaggedProducts().isEmpty()) {
            return List.of();
        }

        return receiptExtractionToReview.flaggedProducts();
    }
}
