package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReprocessShoppingReceiptWithFlaggedProductsDto;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiUnprocessableInputException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenAiShoppingReceiptProcessorAdapterTest {

    @Mock
    private GoogleGenAiChatModel chatModel;

    @Mock
    private GenAiExceptionTranslator genAiExceptionTranslator;

    @Mock
    private PromptBuilder promptBuilder;

    @Mock
    private ReceiptExtractionParser parser;

    @InjectMocks
    private GenAiShoppingReceiptProcessorAdapter adapter;

    @BeforeEach
    void setUp() {
        when(chatModel.getOptions()).thenReturn(GoogleGenAiChatOptions.builder().model("gemini-3.7-flash").build());
    }

    private final MultipartFile file = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", "fake-image-content".getBytes());
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    private final ProcessNewShoppingReceiptDto dto = new ProcessNewShoppingReceiptDto(
            file,
            List.of(),
            clock,
            List.of("DAIRY"),
            List.of("USD"),
            "English"
    );

    private final ReprocessShoppingReceiptWithFlaggedProductsDto reprocessDto = new ReprocessShoppingReceiptWithFlaggedProductsDto(
            "fake-image-content".getBytes(),
            "image/jpeg",
            LocalDate.of(2026, 9, 1),
            "SuperMart",
            List.of(),
            List.of(),
            List.of(),
            clock,
            List.of("DAIRY"),
            List.of("USD"),
            "English"
    );

    @Test
    void process_returnsTheParsedReceiptExtractionOnSuccess() {
        String promptText = "rendered prompt text";
        when(promptBuilder.build(anyString(), eq(dto), any())).thenReturn(promptText);

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        ReceiptExtraction expected = new ReceiptExtraction(LocalDate.of(2026, 9, 1), "SuperMart", null, List.of());
        when(parser.parseAndValidate(eq(response), any())).thenReturn(expected);

        ReceiptExtraction result = adapter.process(dto);

        assertSame(expected, result);
    }

    @Test
    void process_sendsAPromptContainingTheBuiltTextAndTheFileAsMedia() {
        String promptText = "rendered prompt text";
        when(promptBuilder.build(anyString(), eq(dto), any())).thenReturn(promptText);

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.process(dto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        UserMessage userMessage = promptCaptor.getValue().getUserMessage();
        assertEquals(promptText, userMessage.getText());
        assertEquals(1, userMessage.getMedia().size());
        assertEquals("image/jpeg", userMessage.getMedia().getFirst().getMimeType().toString());
    }

    @Test
    void process_requestsJsonResponseMatchingTheConverterSchema() throws com.fasterxml.jackson.core.JsonProcessingException {
        when(promptBuilder.build(anyString(), eq(dto), any())).thenReturn("text");

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.process(dto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        GoogleGenAiChatOptions options = (GoogleGenAiChatOptions) promptCaptor.getValue().getOptions();
        assertEquals("application/json", options.getResponseMimeType());
        assertNotNull(options.getResponseSchema());

        BeanOutputConverter<ReceiptExtraction> expectedConverter = new BeanOutputConverter<>(ReceiptExtraction.class);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedConverter.getJsonSchema()), mapper.readTree(options.getResponseSchema()));
    }

    @Test
    void process_usesTheSameConverterInstanceForPromptAndParsing() {
        when(promptBuilder.build(anyString(), eq(dto), any())).thenReturn("text");

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.process(dto);

        ArgumentCaptor<BeanOutputConverter> promptConverterCaptor = ArgumentCaptor.forClass(BeanOutputConverter.class);
        verify(promptBuilder).build(anyString(), eq(dto), promptConverterCaptor.capture());

        ArgumentCaptor<BeanOutputConverter> parseConverterCaptor = ArgumentCaptor.forClass(BeanOutputConverter.class);
        verify(parser).parseAndValidate(eq(response), parseConverterCaptor.capture());

        assertSame(promptConverterCaptor.getValue(), parseConverterCaptor.getValue());
    }

    @Test
    void process_translatesAndThrowsWhenChatModelFails() {
        when(promptBuilder.build(anyString(), eq(dto), any())).thenReturn("text");

        RuntimeException original = new RuntimeException("boom");
        when(chatModel.call(any(Prompt.class))).thenThrow(original);

        AiUnprocessableInputException translated = new AiUnprocessableInputException("could not be processed");
        when(genAiExceptionTranslator.translate(original)).thenReturn(translated);

        Exception thrown = assertThrows(AiUnprocessableInputException.class, () -> adapter.process(dto));

        assertSame(translated, thrown);
        verifyNoInteractions(parser);
    }

    @Test
    void reprocess_returnsTheParsedReceiptExtractionOnSuccess() {
        String promptText = "rendered prompt text";
        when(promptBuilder.build(anyString(), eq(reprocessDto), any())).thenReturn(promptText);

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);

        ReceiptExtraction expected = new ReceiptExtraction(LocalDate.of(2026, 9, 1), "SuperMart", null, List.of());
        when(parser.parseAndValidate(eq(response), any())).thenReturn(expected);

        ReceiptExtraction result = adapter.reprocess(reprocessDto);

        assertSame(expected, result);
    }

    @Test
    void reprocess_sendsAPromptContainingTheBuiltTextAndTheImageBytesAsMedia() {
        String promptText = "rendered prompt text";
        when(promptBuilder.build(anyString(), eq(reprocessDto), any())).thenReturn(promptText);

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.reprocess(reprocessDto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        UserMessage userMessage = promptCaptor.getValue().getUserMessage();
        assertEquals(promptText, userMessage.getText());
        assertEquals(1, userMessage.getMedia().size());
        assertEquals("image/jpeg", userMessage.getMedia().getFirst().getMimeType().toString());
        assertArrayEquals(reprocessDto.imageBytes(), userMessage.getMedia().getFirst().getDataAsByteArray());
    }

    @Test
    void reprocess_requestsJsonResponseMatchingTheConverterSchema() throws com.fasterxml.jackson.core.JsonProcessingException {
        when(promptBuilder.build(anyString(), eq(reprocessDto), any())).thenReturn("text");

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.reprocess(reprocessDto);

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());

        GoogleGenAiChatOptions options = (GoogleGenAiChatOptions) promptCaptor.getValue().getOptions();
        assertEquals("application/json", options.getResponseMimeType());
        assertNotNull(options.getResponseSchema());

        BeanOutputConverter<ReceiptExtraction> expectedConverter = new BeanOutputConverter<>(ReceiptExtraction.class);

        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(expectedConverter.getJsonSchema()), mapper.readTree(options.getResponseSchema()));
    }

    @Test
    void reprocess_usesTheSameConverterInstanceForPromptAndParsing() {
        when(promptBuilder.build(anyString(), eq(reprocessDto), any())).thenReturn("text");

        ChatResponse response = new ChatResponse(List.of(new Generation(new AssistantMessage("{}"))));
        when(chatModel.call(any(Prompt.class))).thenReturn(response);
        when(parser.parseAndValidate(any(), any())).thenReturn(new ReceiptExtraction(null, null, null, List.of()));

        adapter.reprocess(reprocessDto);

        ArgumentCaptor<BeanOutputConverter> promptConverterCaptor = ArgumentCaptor.forClass(BeanOutputConverter.class);
        verify(promptBuilder).build(anyString(), eq(reprocessDto), promptConverterCaptor.capture());

        ArgumentCaptor<BeanOutputConverter> parseConverterCaptor = ArgumentCaptor.forClass(BeanOutputConverter.class);
        verify(parser).parseAndValidate(eq(response), parseConverterCaptor.capture());

        assertSame(promptConverterCaptor.getValue(), parseConverterCaptor.getValue());
    }

    @Test
    void reprocess_translatesAndThrowsWhenChatModelFails() {
        when(promptBuilder.build(anyString(), eq(reprocessDto), any())).thenReturn("text");

        RuntimeException original = new RuntimeException("boom");
        when(chatModel.call(any(Prompt.class))).thenThrow(original);

        AiUnprocessableInputException translated = new AiUnprocessableInputException("could not be processed");
        when(genAiExceptionTranslator.translate(original)).thenReturn(translated);

        Exception thrown = assertThrows(AiUnprocessableInputException.class, () -> adapter.reprocess(reprocessDto));

        assertSame(translated, thrown);
        verifyNoInteractions(parser);
    }
}
