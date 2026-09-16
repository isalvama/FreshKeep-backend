package com.isalvama.fresh_keep.shared.infrastructure.ai.product_moved_expiration_date_calculator;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductMovedExpirationDateCalculatorPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductException;
import com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer.ReviewPromptBuilder;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ExpirationDateCalculationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OllamaProductMovedExpirationDateCalculatorAdapter implements ProductMovedExpirationDateCalculatorPort {
    private final OllamaChatModel chatModel;
    private final ReviewPromptBuilder reviewPromptBuilder;


    private static final String TEMPLATE_PROMPT_TEXT = """
            You are a food-storage expert calculating a new expiration date for a grocery product that is being moved from one storage spot to another.

            Product: {productName} ({productType})
            Originally purchased on: {shoppingDate}
            Today's date (the date of the move): {today}

            The product has been moved through the following storage spots over time. Each move may have adjusted its expiration date:
            {productChanges}

            The product is currently in:
            {oldStorageSpot}

            It is now being moved to:
            {newStorageSpot}

            To calculate the new expiration date, consider:
            1. How many days have passed since the product was originally purchased ({shoppingDate} to {today}).
            2. The type of the new storage spot and how it affects shelf life compared to the old spot. Use the reference shelf life ranges below as a guide.
            3. Any previous expiration date adjustments from prior moves — do not reset the clock, adjust from the most recent expiration date.

            Rules:
            - If the new spot is colder than the old spot (e.g. FRIDGE -> FREEZER), the product's remaining shelf life should increase.
            - If the new spot is warmer (e.g. FRIDGE -> PANTRY for a dairy product), the remaining shelf life should decrease — potentially to 0 if the product becomes unsafe.
            - Never set an expiration date in the past.
            - Never extend beyond the maximum shelf life for the product type in the new storage spot type.

            Return only the new expiration date following the format detailed:
            {format}
            """;

    @Override
    @Retryable(retryFor = AiRetryableException.class, maxAttempts = 2,  backoff = @Backoff(delay = 1000))
    public LocalDate execute(ProductMovedDto dto) {

        validateNotNull(dto, "ProductMovedDto");

        if (dto.productChanges().isEmpty()){
            throw new ExpirationDateCalculationException("ProductMovedDto has empty productChanges");
        }

        BeanOutputConverter<LocalDate> converter = new BeanOutputConverter<>(LocalDate.class);

        OllamaChatOptions chatOptions = OllamaChatOptions.builder()
                .outputSchema(converter.getJsonSchema())
                .build();
        ChatResponse response;

        try {
            response = chatModel.call(new Prompt(reviewPromptBuilder.build(TEMPLATE_PROMPT_TEXT, dto, converter), chatOptions));
        } catch (RuntimeException e) {
            throw new AiRetryableException("Unable to reach the AI expiration date calculator.", e);
        }

        return parseAndValidate(response, converter);
    }

    private LocalDate parseAndValidate(ChatResponse response, BeanOutputConverter<LocalDate> converter) {

        if (response.getResult() == null) {
            throw new AiRetryableException("The AI expiration date calculator did not return any response.");
        }

        String jsonText = response.getResult().getOutput().getText();

        String trimmedJsonText = jsonText == null ? "" : jsonText.trim();
        if (trimmedJsonText.isBlank()
                || !(trimmedJsonText.startsWith("{") || trimmedJsonText.startsWith("\""))) {
            throw new AiRetryableException("The AI expiration date calculator returned and empty response.");
        }

        LocalDate expirationDate;

        try {
            expirationDate = converter.convert(jsonText);
        } catch (Exception e) {
            throw new AiRetryableException("The AI expiration date calculator answer could not be parsed", e);
        }

        return expirationDate;
    }

    private static <T> void validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new ExpirationDateCalculationException("ProductMovedDto." + fieldName + " cannot be null.");
    }
}
