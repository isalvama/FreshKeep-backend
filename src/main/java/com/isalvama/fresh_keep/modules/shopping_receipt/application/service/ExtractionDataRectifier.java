package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExtractionDataRectifier {

    public ReceiptExtraction rectifyPurchaseDate(RectifyExtractionDto dto) {
        LocalDate rectifiedPurchaseDate = rectifyDate(dto);
        List<ProductExtraction> rectifiedProducts = rectifyProductsDate(dto.extraction(), rectifiedPurchaseDate);
        return new ReceiptExtraction(
                rectifiedPurchaseDate,
                dto.extraction().storeName(),
                dto.extraction().errorReason(),
                rectifiedProducts
        );
    }

    private LocalDate rectifyDate (RectifyExtractionDto dto){
        return dto.extraction().purchaseDate().isAfter(LocalDate.now(dto.clock()))
                ? LocalDate.now(dto.clock())
                : dto.extraction().purchaseDate();
    }

    private List<ProductExtraction> rectifyProductsDate (ReceiptExtraction extraction, LocalDate rectifiedPurchaseDate){
        long purchaseDateCorrectionDays = ChronoUnit.DAYS.between(extraction.purchaseDate(), rectifiedPurchaseDate);
        return extraction.productExtractions().stream().map(p -> withCorrectedExpirationDate(p, purchaseDateCorrectionDays))
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
}
