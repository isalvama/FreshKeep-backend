package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.google.genai.errors.ClientException;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.errors.ServerException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRateLimitedException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiUnprocessableInputException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.TicketProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class GenAiExceptionTranslatorTest {

    private final GenAiExceptionTranslator translator = new GenAiExceptionTranslator();

    @Test
    void translate_mapsGenAiIOExceptionToAiRetryableException() {
        GenAiIOException original = new GenAiIOException("connection refused", new IOException("boom"));

        RuntimeException result = translator.translate(original);

        assertInstanceOf(AiRetryableException.class, result);
        assertTrue(result.getMessage().contains("check your internet connection"));
        assertSame(original, result.getCause());
    }

    @Test
    void translate_mapsServerExceptionToAiRetryableException() {
        ServerException original = new ServerException(500, "INTERNAL", "server down");

        RuntimeException result = translator.translate(original);

        assertInstanceOf(AiRetryableException.class, result);
        assertTrue(result.getMessage().contains("currently unavailable"));
        assertSame(original, result.getCause());
    }

    @Test
    void translate_mapsClientException429ToAiRateLimitedException() {
        ClientException original = new ClientException(429, "RESOURCE_EXHAUSTED", "rate limited");

        RuntimeException result = translator.translate(original);

        assertInstanceOf(AiRateLimitedException.class, result);
        assertTrue(result.getMessage().contains("exceeded the allowed number of requests per minute"));
        assertSame(original, result.getCause());
    }

    @Test
    void translate_mapsClientException401ByThrowingTicketProcessingExceptionDirectly() {
        ClientException original = new ClientException(401, "UNAUTHENTICATED", "bad credentials");

        TicketProcessingException exception = assertThrows(TicketProcessingException.class, () -> translator.translate(original));

        assertTrue(exception.getMessage().contains("Invalid credentials"));
        assertSame(original, exception.getCause());
    }

    @Test
    void translate_mapsClientException403ByThrowingTicketProcessingExceptionDirectly() {
        ClientException original = new ClientException(403, "PERMISSION_DENIED", "forbidden");

        TicketProcessingException exception = assertThrows(TicketProcessingException.class, () -> translator.translate(original));

        assertTrue(exception.getMessage().contains("Invalid credentials"));
        assertSame(original, exception.getCause());
    }

    @Test
    void translate_mapsOtherClientExceptionCodesToAiUnprocessableInputException() {
        ClientException original = new ClientException(400, "INVALID_ARGUMENT", "bad request");

        RuntimeException result = translator.translate(original);

        assertInstanceOf(AiUnprocessableInputException.class, result);
        assertTrue(result.getMessage().contains("could not be processed"));
        assertSame(original, result.getCause());
    }

    @Test
    void translate_mapsUnrecognizedRuntimeExceptionToTicketProcessingException() {
        RuntimeException original = new IllegalStateException("something odd happened");

        RuntimeException result = translator.translate(original);

        assertInstanceOf(TicketProcessingException.class, result);
        assertTrue(result.getMessage().contains("something odd happened"));
        assertSame(original, result.getCause());
    }

    // Spring AI's GoogleGenAiChatModel wraps every failure from the underlying Google SDK call in a
    // generic RuntimeException("Failed to generate content", cause) before it ever reaches this translator -
    // these tests cover that wrapped shape, not just the raw SDK exception types.

    @Test
    void translate_unwrapsCauseToMapWrappedGenAiIOExceptionToAiRetryableException() {
        GenAiIOException cause = new GenAiIOException("connection refused", new IOException("boom"));
        RuntimeException wrapper = new RuntimeException("Failed to generate content", cause);

        RuntimeException result = translator.translate(wrapper);

        assertInstanceOf(AiRetryableException.class, result);
        assertTrue(result.getMessage().contains("check your internet connection"));
        assertSame(cause, result.getCause());
    }

    @Test
    void translate_unwrapsCauseToMapWrappedServerExceptionToAiRetryableException() {
        ServerException cause = new ServerException(500, "INTERNAL", "server down");
        RuntimeException wrapper = new RuntimeException("Failed to generate content", cause);

        RuntimeException result = translator.translate(wrapper);

        assertInstanceOf(AiRetryableException.class, result);
        assertTrue(result.getMessage().contains("currently unavailable"));
        assertSame(cause, result.getCause());
    }

    @Test
    void translate_unwrapsCauseToMapWrappedClientException429ToAiRateLimitedException() {
        ClientException cause = new ClientException(429, "RESOURCE_EXHAUSTED", "rate limited");
        RuntimeException wrapper = new RuntimeException("Failed to generate content", cause);

        RuntimeException result = translator.translate(wrapper);

        assertInstanceOf(AiRateLimitedException.class, result);
        assertTrue(result.getMessage().contains("exceeded the allowed number of requests per minute"));
        assertSame(cause, result.getCause());
    }

    @Test
    void translate_mapsWrapperWithUnrecognizedCauseToTicketProcessingException() {
        RuntimeException wrapper = new RuntimeException("Failed to generate content", new IllegalStateException("something odd happened"));

        RuntimeException result = translator.translate(wrapper);

        assertInstanceOf(TicketProcessingException.class, result);
        assertSame(wrapper, result.getCause());
    }

    @Test
    void translate_mapsWrapperWithNoCauseToTicketProcessingException() {
        RuntimeException wrapper = new RuntimeException("Failed to generate content");

        RuntimeException result = translator.translate(wrapper);

        assertInstanceOf(TicketProcessingException.class, result);
        assertSame(wrapper, result.getCause());
    }
}
