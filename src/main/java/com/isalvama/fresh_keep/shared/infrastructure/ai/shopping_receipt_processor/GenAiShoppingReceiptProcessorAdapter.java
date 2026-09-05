package com.isalvama.fresh_keep.shared.infrastructure.ai.shopping_receipt_processor;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.AiShoppingReceiptProcessorPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReprocessShoppingReceiptWithFlaggedProducts;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.shared.infrastructure.exception.AiRetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class GenAiShoppingReceiptProcessorAdapter implements AiShoppingReceiptProcessorPort {
    private final GoogleGenAiChatModel chatModel;
    private final PromptBuilder promptBuilder;
    private final GenAiExceptionTranslator genAiExceptionTranslator;
    private final ReceiptExtractionParser parser;

    private static final String PROCESS_TEMPLATE_PROMPT_TEXT = """
        Analyze the provided shopping receipt image precisely and extract structured data from it.

        Extract the following receipt-level details:
        - The purchase date shown on the receipt.
        - The store name.

        Then extract the list of food products on the receipt. If the receipt shows several separate units of the same product purchased individually (e.g. a quantity of "6" next to a single milk bottle, or two identical lines for the same chocolate bar), return one product entry per unit, each with identical details. If instead a product is itself sold as a single multi-unit pack (e.g. a six-pack of beer, a 4-pack of yogurt cups sold together), treat that pack as one single product - do not split it into separate units. Use the receipt's own quantity information to tell these two cases apart: a purchase quantity applied to an otherwise singular item should be exploded into repeated entries, while an item whose own name or packaging already describes it as a pack/multipack should stay a single entry.

    For each product entry:
    1. Clean up its name (e.g. turn "YOG.NAT.X4" into "Plain yogurt").
    2. Classify it into one of the following product categories: {productTypes}
    3. Estimate its typical shelf life in days, based on the nature of the product.
    4. Calculate its approximate expiration date by adding that shelf life to the receipt's purchase date.
    5. Extract its price and currency, if shown on the receipt - if a quantity was exploded into several entries, each entry's price must be the price for that single unit, not the total for the line. The currency must be exactly one of the following currency codes (leave it empty if none of them apply): {moneyCurrencies}
    6. Suggest the most suitable storage spot for it, choosing only from the following list of the user's available storage spots, responding with its id (leave it empty if none of them fit):
    {storageSpots}

    If the image is illegible or doesn't appear to be a shopping receipt, set errorReason to a short description of the problem, leave the other receipt-level fields empty, and return an empty product list.
    If the shopping receipt image does not display the purchase date, use today's date instead: {today}.

    {format}

    """;

    private static final String REPROCESS_TEMPLATE_PROMPT_TEXT = """
    Analyze the provided shopping receipt image again to correct a subset of previously extracted products.

    You already produced an initial extraction for this receipt:
    - Purchase date: {purchaseDate}
    - Store name: {storeName}
    - Previously extracted products, in order (do not change any of these values unless the product also appears under "Products to re-examine" below):
    {allProducts}

    The following products were flagged as suspicious during review and must be re-examined carefully against the image, specifically addressing the reason each one was flagged for:
    {productsToReview}

    For each flagged product only, re-derive its data directly from the image following these rules:
    1. Clean up its name (e.g. turn "YOG.NAT.X4" into "Plain yogurt").
    2. Classify it into one of the following product categories: {productTypes}
    3. Estimate its typical shelf life in days, based on the nature of the product.
    4. Calculate its approximate expiration date by adding that shelf life to the receipt's purchase date.
    5. Extract its price and currency, if shown on the receipt. The currency must be exactly one of the following currency codes (leave it empty if none of them apply): {moneyCurrencies}
    6. Suggest the most suitable storage spot for it, choosing only from the following list of the user's available storage spots, responding with its id (leave it empty if none of them fit):
    {storageSpots}
    
    Return the complete list of products for this receipt, in the same order as the previous extraction: unflagged products with their values copied over exactly unchanged, and flagged products replaced with their corrected values. Do not add, remove, duplicate or reorder products - the only fields you may change belong to the flagged products listed above.
    If the shopping receipt image does not display the purchase date, use today's date instead: {today}.
    {format}
    """;

    @Override
    @Retryable(retryFor = AiRetryableException.class, maxAttempts = 2, backoff = @Backoff(delay = 1000))
    public ReceiptExtraction process(ProcessNewShoppingReceiptDto processNewShoppingReceiptDto){

        var converter = new BeanOutputConverter<>(ReceiptExtraction.class);

        String promptText = promptBuilder.build(PROCESS_TEMPLATE_PROMPT_TEXT, processNewShoppingReceiptDto, converter);

        MultipartFile file = processNewShoppingReceiptDto.file();

        var userMessage = UserMessage.builder()
                .text(promptText)
                .media(new Media(MimeType.valueOf(file.getContentType()), file.getResource()))
                .build();

        GoogleGenAiChatOptions chatOptions = GoogleGenAiChatOptions.builder()
                .responseMimeType("application/json")
                .responseSchema(converter.getJsonSchema())
                .build();

        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(userMessage, chatOptions));
        } catch (RuntimeException e) {
            throw genAiExceptionTranslator.translate(e);
        }

        return parser.parseAndValidate(response, converter);
    }

    @Override
    public ReceiptExtraction reprocess(ReprocessShoppingReceiptWithFlaggedProducts processShoppingReceiptFlaggedProdsDto) {

        var converter = new BeanOutputConverter<>(ReceiptExtraction.class);

        String promptText = promptBuilder.build(REPROCESS_TEMPLATE_PROMPT_TEXT, processShoppingReceiptFlaggedProdsDto, converter);

        var userMessage = UserMessage.builder()
                .text(promptText)
                .media(toMedia(processShoppingReceiptFlaggedProdsDto.imageBytes(), processShoppingReceiptFlaggedProdsDto.mimeType()))
                .build();

        GoogleGenAiChatOptions chatOptions = GoogleGenAiChatOptions.builder()
                .responseMimeType("application/json")
                .responseSchema(converter.getJsonSchema())
                .build();

        ChatResponse response;
        try {
            response = chatModel.call(new Prompt(userMessage, chatOptions));
        } catch (RuntimeException e) {
            throw genAiExceptionTranslator.translate(e);
        }

        return parser.parseAndValidate(response, converter);
    }

    private Media toMedia(byte[] imageBytes, String mimeType) {
        return Media.builder()
                .mimeType(MimeType.valueOf(mimeType))
                .data(imageBytes)
                .build();
    }
}
