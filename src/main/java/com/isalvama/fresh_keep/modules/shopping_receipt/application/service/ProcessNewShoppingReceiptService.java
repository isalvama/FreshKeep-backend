package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessNewShoppingReceiptService implements ProcessNewShoppingReceiptUseCase {
    private final SpaceLookUpPort spaceLookUpPort;
    private final ProductCategoriesLookUpPort productCategoriesLookUpPort;
    private final LanguageResolver languageResolver;
    private final AiShoppingReceiptProcessorPort shoppingReceiptProcessorPort;
    private final AiReceiptExtractionReviewerPort extractionReviewerPort;
    private final ImageStoragePort imageStoragePort;
    private final ReceiptImageRepositoryPort receiptImageRepositoryPort;
    private final ExtractionDataRectifier extractionDataRectifier;
    private final StorageSpotSuggestionResolver spotSuggestionResolver;
    private final Clock clock;

    @Override
    public ProcessNewShoppingReceiptResult execute(ProcessNewShoppingReceiptCommand command) {
        if (command.file().isEmpty()){
            throw new InvalidReceiptImageException("File cannot be empty.");
        }

        List<StorageSpotDto> storageSpotDtos = spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(
                GetStorageSpotsDto.create(command.spaceId(), command.creatorId())
        );
        CategoriesDto categories = productCategoriesLookUpPort.getProductTypesAndMoneyCurrencyConstNames();

        ReceiptExtraction extraction = shoppingReceiptProcessorPort.process(
                new ProcessNewShoppingReceiptDto(
                        command.file(),
                        storageSpotDtos,
                        clock,
                        categories.productTypes(),
                        categories.moneyCurrencies(),
                        languageResolver.resolve(command.language())
                ));

        ReceiptExtraction rectifiedExtraction = extractionDataRectifier.rectifyPurchaseDate(new RectifyExtractionDto(extraction, storageSpotDtos, clock));
        List<ProductExtraction> productExtractions = spotSuggestionResolver.resolve(rectifiedExtraction.productExtractions(), storageSpotDtos);

        List<ProductReviewFlag> productsToReview;
        try {
            productsToReview = extractionReviewerPort.review(
                    new ReviewNewShoppingReceiptDto(storageSpotDtos, productExtractions, rectifiedExtraction.purchaseDate()));
        } catch (InfrastructureException e){
            productsToReview = List.of();
            log.warn("The AiReceiptExtractionReviewerPort.execute() threw an exception with the following message: {}. productsToReview is initialized as an empty list.", e.getMessage());
        }

        String avatarAssetId = imageStoragePort.upload(command.file(), "shopping_receipts/receipts");

        ReceiptImage receiptImage = ReceiptImage.create(AssetId.of(avatarAssetId), command.file().getContentType());

        receiptImageRepositoryPort.save(receiptImage);

        return new ProcessNewShoppingReceiptResult(
                receiptImage.getId().toString(),
                storageSpotDtos.stream().map(SuggestedStorageSpotResult::fromStorageSpotDto).toList(),
                rectifiedExtraction.purchaseDate(),
                rectifiedExtraction.storeName(),
                productExtractions == null || productExtractions.isEmpty()
                    ? List.of()
                    : productExtractions.stream().map(ProcessProductResult::fromProductExtraction).toList(),
                productsToReview == null || productsToReview.isEmpty() ? List.of() : productsToReview.stream().map(p -> ProcessProductResult.fromProductExtraction(p.product())).toList()
        );
    }

}
