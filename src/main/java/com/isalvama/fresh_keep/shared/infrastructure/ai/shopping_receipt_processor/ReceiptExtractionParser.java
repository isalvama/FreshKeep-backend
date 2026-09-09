package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
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
        boolean hasProducts = receiptExtraction.productExtractions() != null && !receiptExtraction.productExtractions().isEmpty();
        if (hasProducts) {
            return;
        }

        // Per the prompt's own contract, a genuine error always comes with an empty product list - errorReason
        // is required and non-nullable in the response schema, so the model represents "no error" with all
        // sorts of placeholder values ("", "none", ...) rather than reliably using JSON null. Since an empty
        // product list is itself the authoritative signal of a real error, errorReason is only used here to
        // explain why (when it looks like a real explanation), never to detect whether there was an error.
        String reason = receiptExtraction.errorReason();
        throw new AiUnprocessableInputException(reason != null && !reason.isBlank()
                ? reason
                : "Unable to extract food products from the image.");
    }
}
