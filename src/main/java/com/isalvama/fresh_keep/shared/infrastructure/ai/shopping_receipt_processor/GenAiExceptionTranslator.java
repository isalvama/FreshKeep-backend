package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRateLimitedException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiUnprocessableInputException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.TicketProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GenAiExceptionTranslator {

    public RuntimeException translate(RuntimeException e) {
        if (e instanceof GenAiIOException genAiIOException) {
            return new AiRetryableException("Unable to connect to the AI service. Please check your internet connection.", genAiIOException);
        }
        if (e instanceof ServerException serverException) {
            return new AiRetryableException("The AI service is currently unavailable. Please try again in a few minutes.", serverException);
        }
        if (e instanceof ClientException clientException) {
            return translateClientException(clientException);
        }
        return new TicketProcessingException("An unexpected error occurred while processing the receipt image: " + e.getMessage(), e);
    }

    private RuntimeException translateClientException(ClientException e) {
        return switch (e.code()) {
            case 429 -> new AiRateLimitedException("We’ve exceeded the allowed number of requests per minute. Please wait a moment and try again.", e);
            case 401, 403 -> {
                log.error("Internal system configuration error (Invalid credentials). " + e.getMessage() + ". Cause: " + e.getCause());
                throw new TicketProcessingException("Internal system configuration error (Invalid credentials).", e);
            }
            default -> new AiUnprocessableInputException("The image was rejected or could not be processed. Make sure it contains only a purchase receipt and no sensitive information.", e);
        };
    }
}
