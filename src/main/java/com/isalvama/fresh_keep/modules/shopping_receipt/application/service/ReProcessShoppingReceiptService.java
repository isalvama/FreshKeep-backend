package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ReProcessShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ProcessReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifiedReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ReProcessShoppingReceiptService implements ReProcessShoppingReceiptUseCase {
    private final SpaceLookUpPort spaceLookUpPort;
    private final ProductCategoriesLookUpPort productCategoriesLookUpPort;
    private final ReceiptImageRepositoryPort receiptImageRepositoryPort;
    private final ImageStoragePort imageStoragePort;
    private final LanguageResolver languageResolver;
    private final AiShoppingReceiptProcessorPort aiShoppingReceiptProcessorPort;
    private final ExtractionDataRectifier extractionDataRectifier;
    private final StorageSpotSuggestionResolver storageSpotResolver;
    private final ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    private final ProductRegistrationPort productRegistrationPort;
    private final Clock clock;

    @Override
    public ShoppingReceiptResult execute (ReProcessShoppingReceiptCommand command) {

        List<StorageSpotDto> storageSpotDtos = spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(
                GetStorageSpotsDto.create(command.spaceId(), command.creatorId())
        );
        CategoriesDto categories = productCategoriesLookUpPort.getProductTypesAndMoneyCurrencyConstNames();

        ReceiptImage receiptImage = receiptImageRepositoryPort.findById(ReceiptImageId.from(command.receiptImageId()))
                .orElseThrow(() -> new NonExistentReceiptImageException("Receipt Image with id " + command.receiptImageId() + " does not exist."));

        String imageUrl = imageStoragePort.retrieveUrl(receiptImage.getAssetId().toString());
        byte[] imageBytes = imageStoragePort.fetchImageBytes(imageUrl);

        ShoppingReceipt shoppingReceipt = shoppingReceiptRepositoryPort.getById(ShoppingReceiptId.from(command.shoppingReceiptId()))
                .orElseThrow(() -> new NonExistentShoppingReceiptException("Shopping Receipt with id " + command.shoppingReceiptId() + " does not exist."));
        if (!shoppingReceipt.getCreatorId().toString().equals(command.creatorId()) || !shoppingReceipt.getReceiptImageId().toString().equals(command.receiptImageId()) || !shoppingReceipt.getSpaceId().toString().equals(command.spaceId())) {
            throw new InvalidShoppingReceiptException("The shopping receipt cannot be reprocessed in the requested context.");
        }

        ReceiptExtraction extraction = aiShoppingReceiptProcessorPort.reprocess(new ReprocessShoppingReceiptWithFlaggedProductsDto(
                        imageBytes,
                        receiptImage.getMimeType(),
                        command.shoppingDate(),
                        command.storeName(),
                        command.allProducts().stream().map(this::toProductExtraction).toList(),
                        command.flaggedProducts().stream().map(this::toProductExtraction).toList(),
                        storageSpotDtos,
                        clock,
                        categories.productTypes(),
                        categories.moneyCurrencies(),
                        languageResolver.resolve(command.language())
                )
        );

        RectifiedReceiptDto rectifiedReceipt = extractionDataRectifier.rectifyPurchaseDate(
                ProcessReceiptToRectifyDto.from(extraction, clock)
        );
        ReceiptExtraction rectifiedExtraction = ReceiptExtraction.from(
                rectifiedReceipt,
                extraction.storeName(),
                extraction.errorReason()
        );
        List<ProductExtraction> productExtractions = storageSpotResolver.resolve(rectifiedExtraction.productExtractions(), storageSpotDtos);

        shoppingReceipt.confirm(rectifiedExtraction.purchaseDate(), rectifiedExtraction.storeName());
        shoppingReceiptRepositoryPort.update(shoppingReceipt);

        List<RegisteredProductDto> registeredProducts = productRegistrationPort.registerProducts(
                productExtractions.stream().map(pe ->
                        new RegisterProductDto(
                                pe.productName(),
                                pe.expirationDate(),
                                pe.suggestedStorageSpotId(),
                                pe.productType(),
                                shoppingReceipt.getId().toString(),
                                pe.priceAmount(),
                                pe.currency(),
                                UUID.fromString(command.creatorId())
                        )).toList()
        );

        List<RegisteredProductDto> registeredSortedProducts = registeredProducts.stream().sorted(Comparator.comparing(RegisteredProductDto::expirationDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList();

        return new ShoppingReceiptResult(
                shoppingReceipt.getId().toString(),
                shoppingReceipt.getPurchaseDate(),
                shoppingReceipt.getStoreName(),
                registeredSortedProducts.stream().map(ProductResult::toResult).toList(),
                storageSpotDtos.stream().map(SuggestedStorageSpotResult::fromStorageSpotDto).toList()
                );
    }


    private ProductExtraction toProductExtraction(ProductCommand c) {
        return new ProductExtraction(
                c.expirationDate(), c.productName(), c.suggestedStorageSpotId(),
                c.productType(), c.priceAmount(), c.currency()
        );
    }
}
