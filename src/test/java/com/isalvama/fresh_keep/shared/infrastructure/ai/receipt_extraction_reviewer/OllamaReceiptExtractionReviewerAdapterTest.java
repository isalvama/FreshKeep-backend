package com.isalvama.fresh_keep.shared.infrastructure.ai.receipt_extraction_reviewer;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReviewNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtractionToReview;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaReceiptExtractionReviewerAdapterTest {

    @Mock
    private OllamaChatModel chatModel;

    @Mock
    private ReviewPromptBuilder reviewPromptBuilder;

    @InjectMocks
    private OllamaReceiptExtractionReviewerAdapter adapter;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    private final List<StorageSpotDto> storageSpots = List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"));

    private final List<ProductExtraction> products = List.of(
            new ProductExtraction(LocalDate.of(2026, 9, 10), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD")
    );

    private final ReviewNewShoppingReceiptDto dto = new ReviewNewShoppingReceiptDto(storageSpots, products, LocalDate.of(2026, 9, 1));

    @Test
    void review_returnsEmptyListWhenDtoIsNull() {
        List<ProductReviewFlag> result = adapter.review(null);

        assertEquals(List.of(), result);
        verifyNoInteractions(reviewPromptBuilder, chatModel);
    }

    @Test
    void review_returnsEmptyListWhenProductExtractionsIsNull() {
        List<ProductReviewFlag> result = adapter.review(new ReviewNewShoppingReceiptDto(storageSpots, null, LocalDate.of(2026, 9, 1)));

        assertEquals(List.of(), result);
        verifyNoInteractions(reviewPromptBuilder, chatModel);
    }

    @Test
    void review_returnsEmptyListWhenProductExtractionsIsEmpty() {
        List<ProductReviewFlag> result = adapter.review(new ReviewNewShoppingReceiptDto(storageSpots, List.of(), LocalDate.of(2026, 9, 1)));

        assertEquals(List.of(), result);
        verifyNoInteractions(reviewPromptBuilder, chatModel);
    }

    @Test
    void review_returnsEmptyListWhenShoppingDateIsNull() {
        List<ProductReviewFlag> result = adapter.review(new ReviewNewShoppingReceiptDto(storageSpots, products, null));

        assertEquals(List.of(), result);
        verifyNoInteractions(reviewPromptBuilder, chatModel);
    }

    @Test
    void review_returnsFlaggedProductsFromAValidResponse() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        String json = """
                {
                  "flaggedProducts": [
                    {
                      "product": {
                        "expirationDate": "2026-09-10",
                        "productName": "Milk",
                        "suggestedStorageSpotId": "fridge-id",
                        "productType": "DAIRY",
                        "priceAmount": 2.5,
                        "currency": "USD"
                      },
                      "reason": "expiration date looks implausibly far away"
                    }
                  ]
                }
                """;
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(json)))));

        List<ProductReviewFlag> result = adapter.review(dto);

        assertEquals(1, result.size());
        assertEquals("Milk", result.getFirst().product().productName());
        assertEquals("expiration date looks implausibly far away", result.getFirst().reason());
    }

    @Test
    void review_returnsEmptyListWhenNoProductsAreFlagged() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        String json = """
                { "flaggedProducts": [] }
                """;
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage(json)))));

        List<ProductReviewFlag> result = adapter.review(dto);

        assertEquals(List.of(), result);
    }

    @Test
    void review_asksThePromptBuilderToRenderTheTemplateWithTheGivenDto() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{ \"flaggedProducts\": [] }")))));

        adapter.review(dto);

        verify(reviewPromptBuilder).build(anyString(), eq(dto), any());
    }

    @Test
    void review_sendsThePromptBuilderOutputToTheChatModel() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{ \"flaggedProducts\": [] }")))));

        adapter.review(dto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        assertEquals("built prompt text", promptCaptor.getValue().getContents());
    }

    @Test
    void review_requestsAnOutputSchemaMatchingTheConverter() throws com.fasterxml.jackson.core.JsonProcessingException {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{ \"flaggedProducts\": [] }")))));

        adapter.review(dto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        OllamaChatOptions options = (OllamaChatOptions) promptCaptor.getValue().getOptions();
        BeanOutputConverter<ReceiptExtractionToReview> expectedConverter = new BeanOutputConverter<>(ReceiptExtractionToReview.class);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedConverter.getJsonSchema()), mapper.readTree(options.getOutputSchema()));
    }

    @Test
    void review_throwsAiRetryableExceptionWhenChatModelFails() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        RuntimeException original = new RuntimeException("connection refused");
        when(chatModel.call(any(Prompt.class))).thenThrow(original);

        Exception exception = assertThrows(AiRetryableException.class, () -> adapter.review(dto));

        assertTrue(exception.getMessage().contains("Unable to reach the AI reviewer"));
        assertSame(original, exception.getCause());
    }

    @Test
    void review_throwsAiRetryableExceptionWhenChatResponseHasNoResult() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of()));

        Exception exception = assertThrows(AiRetryableException.class, () -> adapter.review(dto));

        assertTrue(exception.getMessage().contains("did not return any response"));
    }

    @Test
    void review_throwsAiRetryableExceptionWhenResponseTextIsBlank() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("")))));

        Exception exception = assertThrows(AiRetryableException.class, () -> adapter.review(dto));

        assertTrue(exception.getMessage().contains("empty response"));
    }

    @Test
    void review_throwsAiRetryableExceptionWhenResponseTextIsNotJson() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("not json")))));

        Exception exception = assertThrows(AiRetryableException.class, () -> adapter.review(dto));

        assertTrue(exception.getMessage().contains("empty response"));
    }

    @Test
    void review_throwsAiRetryableExceptionWhenResponseCannotBeParsed() {
        when(reviewPromptBuilder.build(anyString(), eq(dto), any())).thenReturn("built prompt text");
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("{invalid: json}")))));

        Exception exception = assertThrows(AiRetryableException.class, () -> adapter.review(dto));

        assertTrue(exception.getMessage().contains("could not be parsed"));
    }
}
