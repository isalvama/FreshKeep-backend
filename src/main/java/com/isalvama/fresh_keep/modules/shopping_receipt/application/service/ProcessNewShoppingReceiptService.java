package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductReviewFlag;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
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
    private final AiShoppingReceiptProcessorPort shoppingReceiptProcessorPort;
    private final AiReceiptExtractionReviewerPort extractionReviewerPort;
    private final ImageStoragePort imageStoragePort;
    private final ReceiptImageRepositoryPort receiptImageRepositoryPort;
    private final ExtractionDataRectifier extractionDataRectifier;
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
                        categories.moneyCurrencies()
                ));

        ReceiptExtraction rectifiedExtraction = extractionDataRectifier.rectify(new RectifyExtractionDto(extraction, storageSpotDtos, clock));

        List<ProductReviewFlag> productsToReview;
        try {
            productsToReview = extractionReviewerPort.review(
                    new ReviewNewShoppingReceiptDto(storageSpotDtos, rectifiedExtraction.productExtractions(), rectifiedExtraction.purchaseDate()));
        } catch (InfrastructureException e){
            productsToReview = List.of();
            log.warn("The AiReceiptExtractionReviewerPort.execute() threw an exception with the following message: {}. productsToReview is initialized as an empty list.", e.getMessage());
        }

        String avatarAssetId = imageStoragePort.upload(command.file(), "shopping_receipts/receipts");

        ReceiptImage receiptImage = ReceiptImage.create(AssetId.of(avatarAssetId));

        receiptImageRepositoryPort.save(receiptImage);

        return new ProcessNewShoppingReceiptResult(
                receiptImage.getId().toString(),
                storageSpotDtos.stream().map(SuggestedStorageSpotResult::fromStorageSpotDto).toList(),
                rectifiedExtraction.purchaseDate(),
                rectifiedExtraction.storeName(),
                rectifiedExtraction.productExtractions() == null || rectifiedExtraction.productExtractions().isEmpty()
                    ? List.of()
                    : rectifiedExtraction.productExtractions().stream().map(ProductResult::fromProductExtraction).toList(),
                productsToReview == null || productsToReview.isEmpty() ? List.of() : productsToReview.stream().map(p -> ProductResult.fromProductExtraction(p.product())).toList()
        );
    }

}
