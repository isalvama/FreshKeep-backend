package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductCategoriesLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.StorageSpotDto;
import com.isalvama.fresh_keep.shared.domain.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class StorageSpotSuggestionResolver {
    private final ProductCategoriesLookUpPort productCategoriesLookUpPort;

    public List<ProductExtraction> resolve (List<ProductExtraction> products, List<StorageSpotDto> storageSpots){
        Set<String> storageSpotIds = storageSpots.stream().map(StorageSpotDto::id).collect(Collectors.toSet());
        return products.stream()
                .map(p -> storageSpotIds.contains(p.suggestedStorageSpotId())
                        ? p
                        : withoutStorageSpotSuggestion(p, storageSpots)
                ).toList();
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
            log.warn("StorageSpotSuggestionResolver.resolve threw a DomainException with the following message: " + e.getMessage() + ". Flow continues.");
        }
        return new ProductExtraction(p.expirationDate(), p.productName(), fallbackStorageSpotId, p.productType(), p.priceAmount(), p.currency());
    }
}
