package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductCategoriesLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.domain.exception.DomainException;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionDataRectifier {
    private final ProductCategoriesLookUpPort productCategoriesLookUpPort;

    public ReceiptExtraction rectify(RectifyExtractionDto dto){
        LocalDate rectifiedPurchaseDate = rectifyPurchaseDate(dto.extraction().purchaseDate(), dto.clock());
        List<ProductExtraction> rectifiedProducts = rectifyProductsData(dto.extraction(), dto.storageSpots(), rectifiedPurchaseDate);
        return new ReceiptExtraction(
                rectifiedPurchaseDate,
                dto.extraction().storeName(),
                dto.extraction().errorReason(),
                rectifiedProducts
                );
    }

    private LocalDate rectifyPurchaseDate(LocalDate purchaseDate, Clock clock) {
        return purchaseDate.isAfter(LocalDate.now(clock))
                ? LocalDate.now(clock)
                : purchaseDate;
    }

    private List<ProductExtraction> rectifyProductsData(ReceiptExtraction extraction, List<StorageSpotDto> storageSpotDtos, LocalDate rectifiedPurchaseDate) {
        long purchaseDateCorrectionDays = ChronoUnit.DAYS.between(extraction.purchaseDate(), rectifiedPurchaseDate);
        Set<String> storageSpotIds = storageSpotDtos.stream().map(StorageSpotDto::id).collect(Collectors.toSet());

        return extraction.productExtractions().stream()
                .map(p -> storageSpotIds.contains(p.suggestedStorageSpotId())
                        ? p
                        : withoutStorageSpotSuggestion(p, storageSpotDtos)
                )
                .map(p -> withCorrectedExpirationDate(p, purchaseDateCorrectionDays))
                .toList();
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
                p.currency()
        );
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
        } catch (DomainException e) {
            log.warn("ProcessNewShoppingReceiptService.withoutStorageSpotSuggestion() method threw a DomainException with the following message: " + e.getMessage() + ". Flow continues.");
        }
        return new ProductExtraction(p.expirationDate(), p.productName(), fallbackStorageSpotId, p.productType(), p.priceAmount(), p.currency());
    }


}
