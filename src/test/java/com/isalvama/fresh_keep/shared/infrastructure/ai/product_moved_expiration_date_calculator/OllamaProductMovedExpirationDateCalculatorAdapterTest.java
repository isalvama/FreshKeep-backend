package com.isalvama.fresh_keep.shared.infrastructure.ai.product_moved_expiration_date_calculator;

import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductChangesDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer.ReviewPromptBuilder;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.ExpirationDateCalculationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaProductMovedExpirationDateCalculatorAdapterTest {

    @Mock
    private OllamaChatModel chatModel;

    @Mock
    private ReviewPromptBuilder reviewPromptBuilder;

    @InjectMocks
    private OllamaProductMovedExpirationDateCalculatorAdapter adapter;

    private final ProductMovedDto dto = new ProductMovedDto(
            "Milk", "DAIRY", LocalDate.of(2026, 9, 1),
            List.of(new ProductChangesDto("fridge-id", "Fridge", "FRIDGE",
                    LocalDateTime.of(2026, 9, 5, 10, 0), LocalDate.of(2026, 9, 10))),
            StorageSpotInfoDto.create("pantry-id", "Pantry", "PANTRY"),
            StorageSpotInfoDto.create("fridge-id", "Fridge", "FRIDGE"),
            Clock.fixed(Instant.parse("2026-09-16T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void execute_returnsTheDateFromAValidChatResponse() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt");
        when(chatModel.call(any(Prompt.class))).thenReturn(response("\"2026-09-20\""));

        LocalDate result = adapter.execute(dto);

        assertEquals(LocalDate.of(2026, 9, 20), result);
    }

    @Test
    void execute_throwsExpirationDateCalculationExceptionWhenDtoIsNull() {
        Exception exception = assertThrows(ExpirationDateCalculationException.class, () -> adapter.execute(null));

        assertTrue(exception.getMessage().contains("ProductMovedDto"));
        verifyNoInteractions(reviewPromptBuilder, chatModel);
    }

    @Test
    void execute_wrapsChatModelFailuresAsRetryable() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt");
        RuntimeException original = new RuntimeException("connection refused");
        when(chatModel.call(any(Prompt.class))).thenThrow(original);

        AiRetryableException exception = assertThrows(AiRetryableException.class, () -> adapter.execute(dto));

        assertSame(original, exception.getCause());
    }

    @Test
    void execute_throwsRetryableExceptionWhenChatResponseHasNoResult() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of()));

        assertThrows(AiRetryableException.class, () -> adapter.execute(dto));
    }

    @Test
    void execute_throwsRetryableExceptionWhenChatResponseCannotBeParsed() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt");
        when(chatModel.call(any(Prompt.class))).thenReturn(response("not json"));

        assertThrows(AiRetryableException.class, () -> adapter.execute(dto));
    }

    private ChatResponse response(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
