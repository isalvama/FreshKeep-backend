package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.AiReceiptExtractionReviewerPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.AiShoppingReceiptProcessorPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductCategoriesLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.CategoriesDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.GetStorageSpotsDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import com.isalvama.fresh_keep.shared.domain.exception.DomainException;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ProductExtraction;
import com.isalvama.fresh_keep.shared.infrastructure.ai.dto.ReceiptExtraction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProcessNewShoppingReceiptService implements ProcessNewShoppingReceiptUseCase {
    private final SpaceLookUpPort spaceLookUpPort;
    private final ProductCategoriesLookUpPort productCategoriesLookUpPort;
    private final AiShoppingReceiptProcessorPort shoppingReceiptProcessorPort;
    private final AiReceiptExtractionReviewerPort extractionReviewerPort;
    private final Clock clock;

    @Override
    public void execute(ProcessNewShoppingReceiptCommand command) {

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

        Set<String> storageSpotIds = storageSpotDtos.stream().map(StorageSpotDto::id).collect(Collectors.toSet());

        LocalDate purchaseDate = extraction.purchaseDate().isAfter(LocalDate.now(clock))
                ? LocalDate.now(clock)
                : extraction.purchaseDate();
        long purchaseDateCorrectionDays = ChronoUnit.DAYS.between(extraction.purchaseDate(), purchaseDate);

        List<ProductExtraction> products = extraction.productExtractions().stream()
                .map(p -> storageSpotIds.contains(p.suggestedStorageSpotId())
                        ? p
                        : withoutStorageSpotSuggestion(p, storageSpotDtos)
                )
                .map(p -> withCorrectedExpirationDate(p, purchaseDateCorrectionDays))
                .toList();

        ShoppingReceipt shoppingReceipt = ShoppingReceipt.create(UserId.from(command.creatorId()), SpaceId.from(command.spaceId()), purchaseDate, extraction.storeName(), clock);

        // TODO review by Ollama

        List<ProductExtraction> reviewedProducts = extractionReviewerPort.review(products);

        List<ProductExtraction> productsToReview;

        // Upload shopping receipt image by Cloudinary
    }

    private ProductExtraction withoutStorageSpotSuggestion(ProductExtraction p, List<StorageSpotDto> storageSpotDtos) {
        String fallbackStorageSpotId = null;

        try {
            String preferredStorageSpotType = productCategoriesLookUpPort.getPreferredStorageSpotTypeFor(p.productType());
            fallbackStorageSpotId = storageSpotDtos.stream()
                    .filter(s -> s.type().equalsIgnoreCase(preferredStorageSpotType))
                    .findFirst()
                    .map(StorageSpotDto::id)
                    .orElse(null);
        } catch (DomainException e) {}
        return new ProductExtraction(p.expirationDate(), p.productName(), fallbackStorageSpotId, p.productType(), p.priceAmount(), p.money());
    }

    private ProductExtraction withCorrectedExpirationDate(ProductExtraction p, long purchaseDateCorrectionDays) {
        if (purchaseDateCorrectionDays == 0 || p.expirationDate() == null) {
            return p;
        }
        return new ProductExtraction(
                p.expirationDate().plusDays(purchaseDateCorrectionDays),
                p.productName(),
                p.suggestedStorageSpotId(),
                p.productType(),
                p.priceAmount(),
                p.money()
        );
    }
}
