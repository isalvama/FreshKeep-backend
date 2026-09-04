package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiUnprocessableInputException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.converter.BeanOutputConverter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ReceiptExtractionParserTest {
    private BeanOutputConverter<ReceiptExtraction> converter = new BeanOutputConverter<>(ReceiptExtraction.class);
    private ReceiptExtractionParser parser = new ReceiptExtractionParser();

    @Test
    void extractResponse_shouldThrowAiUnprocessableInputException(){
        ChatResponse nullResultResponse = new ChatResponse(List.of());

        Exception exc = assertThrows(AiUnprocessableInputException.class, () -> parser.parseAndValidate(nullResultResponse, converter));

        assertTrue(exc.getMessage().contains("AI"));
        assertTrue(exc.getMessage().contains("not return"));
        assertTrue(exc.getMessage().contains("any response"));
        assertTrue(exc.getMessage().contains("ticket"));
    }

    @Test
    void extractResponse_shouldThrowRetryableExceptionNullText(){
        ChatResponse nullTextResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(null))));

        Exception exc = assertThrows(AiRetryableException.class, () -> parser.parseAndValidate(nullTextResponse, converter));

        assertTrue(exc.getMessage().contains("ticket"));
        assertTrue(exc.getMessage().contains("not"));
        assertTrue(exc.getMessage().contains("be interpreted"));
        assertTrue(exc.getMessage().contains("text is legible"));
    }

    @Test
    void extractResponse_shouldThrowRetryableExceptionBlankText(){
        ChatResponse blankTextResponse = new ChatResponse(List.of(new Generation(new AssistantMessage(""))));

        Exception exc = assertThrows(AiRetryableException.class, () -> parser.parseAndValidate(blankTextResponse, converter));

        assertTrue(exc.getMessage().contains("ticket"));
        assertTrue(exc.getMessage().contains("not"));
        assertTrue(exc.getMessage().contains("be interpreted"));
        assertTrue(exc.getMessage().contains("text is legible"));
    }

    @Test
    void extractResponse_shouldThrowRetryableExceptionWhenTextIsNotAJson(){
        ChatResponse NotJsonTextResponse = new ChatResponse(List.of(new Generation(new AssistantMessage("a"))));

        Exception exc = assertThrows(AiRetryableException.class, () -> parser.parseAndValidate(NotJsonTextResponse, converter));

        assertTrue(exc.getMessage().contains("ticket"));
        assertTrue(exc.getMessage().contains("not"));
        assertTrue(exc.getMessage().contains("be interpreted"));
        assertTrue(exc.getMessage().contains("text is legible"));
    }

    @Test
    void extractResponse_shouldThrowAiRetryableExceptionWhenFormatOfTextDoesNotMatchConverterType(){
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{format : invalid}"))));

        Exception exc = assertThrows(AiRetryableException.class, () -> parser.parseAndValidate(response, converter));

        assertTrue(exc.getMessage().contains("ticket format"));
        assertTrue(exc.getMessage().contains("not compatible"));
        assertTrue(exc.getMessage().contains("system"));
    }

    @Test
    void parseAndValidate_returnsReceiptExtractionWhenResponseIsValid() {
        String json = """
                {
                  "purchaseDate": "2026-09-01",
                  "storeName": "SuperMart",
                  "errorReason": null,
                  "productExtractions": [
                    {
                      "expirationDate": "2026-09-10",
                      "productName": "Milk",
                      "suggestedStorageSpotId": "fridge-id",
                      "productType": "DAIRY",
                      "priceAmount": 2.5,
                      "currency": "USD"
                    }
                  ]
                }
                """;
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));

        ReceiptExtraction result = parser.parseAndValidate(response, converter);

        assertEquals(LocalDate.of(2026, 9, 1), result.purchaseDate());
        assertEquals("SuperMart", result.storeName());
        assertNull(result.errorReason());
        assertEquals(1, result.productExtractions().size());
        assertEquals("Milk", result.productExtractions().get(0).productName());
        assertEquals("fridge-id", result.productExtractions().get(0).suggestedStorageSpotId());
        assertEquals(LocalDate.of(2026, 9, 10), result.productExtractions().get(0).expirationDate());
    }

    @Test
    void validateContent_shouldThrowAiUnprocessableInputExceptionWhenErrorReasonIsPresent() {
        String json = """
                {
                  "purchaseDate": null,
                  "storeName": null,
                  "errorReason": "The image is not a receipt",
                  "productExtractions": []
                }
                """;
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));

        Exception exc = assertThrows(AiUnprocessableInputException.class, () -> parser.parseAndValidate(response, converter));

        assertEquals("The image is not a receipt", exc.getMessage());
    }

    @Test
    void validateContent_shouldThrowAiUnprocessableInputExceptionWhenProductExtractionsIsNull() {
        String json = """
                {
                  "purchaseDate": "2026-09-01",
                  "storeName": "SuperMart",
                  "errorReason": null,
                  "productExtractions": null
                }
                """;
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));

        Exception exc = assertThrows(AiUnprocessableInputException.class, () -> parser.parseAndValidate(response, converter));

        assertTrue(exc.getMessage().contains("Unable"));
        assertTrue(exc.getMessage().contains("extract"));
        assertTrue(exc.getMessage().contains("food products"));
        assertTrue(exc.getMessage().contains("image"));

    }

    @Test
    void validateContent_shouldThrowAiUnprocessableInputExceptionWhenProductExtractionsIsEmpty() {
        String json = """
                {
                  "purchaseDate": "2026-09-01",
                  "storeName": "SuperMart",
                  "errorReason": null,
                  "productExtractions": []
                }
                """;
        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage(json))));

        Exception exc = assertThrows(AiUnprocessableInputException.class, () -> parser.parseAndValidate(response, converter));

        assertTrue(exc.getMessage().contains("Unable"));
        assertTrue(exc.getMessage().contains("extract"));
        assertTrue(exc.getMessage().contains("food products"));
        assertTrue(exc.getMessage().contains("image"));
    }

}