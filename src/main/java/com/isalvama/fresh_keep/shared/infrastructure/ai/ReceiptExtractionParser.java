package com.isalvama.fresh_keep.shared.infrastructure.ai;

import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiUnprocessableInputException;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Component
public class ReceiptExtractionParser {
    public ReceiptExtraction parseAndValidate(ChatResponse response, BeanOutputConverter<ReceiptExtraction> converter){

        String jsonText = extractResponse(response);

        ReceiptExtraction receiptExtraction = parse(jsonText, converter);

        validateContent(receiptExtraction);

        return receiptExtraction;
    }

    private String extractResponse(ChatResponse response){
        if (response.getResult() == null) {
            throw new AiUnprocessableInputException("The AI did not return any response to the ticket");
        }

        String jsonText = response.getResult().getOutput().getText();

        if (jsonText == null || jsonText.isBlank() || !jsonText.trim().startsWith("{")) {
            throw new AiRetryableException("The ticket could not be interpreted. Make sure the text is legible in the image.");
        }

        return jsonText;
    }

    private ReceiptExtraction parse (String jsonText, BeanOutputConverter<ReceiptExtraction> converter){
        try {
            return converter.convert(jsonText);
        } catch (Exception e) {
            throw new AiRetryableException("The ticket format is not compatible with the system.", e);
        }
    }

    private void validateContent(ReceiptExtraction receiptExtraction){
        if (receiptExtraction.errorReason() != null) {
            throw new AiUnprocessableInputException(receiptExtraction.errorReason());
        }

        if (receiptExtraction.productExtractions() == null || receiptExtraction.productExtractions().isEmpty()) {
            throw new AiUnprocessableInputException("Unable to extract food products from the image.");
        }
    }
}
