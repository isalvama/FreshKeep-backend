package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ConfirmReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ProcessReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ProductsToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifiedReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProductExtraction;
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

    public RectifiedReceiptDto rectifyPurchaseDate(ProcessReceiptToRectifyDto dto) {
        return rectifyProductsDate(rectifyDate(dto));
    }

    public RectifiedReceiptDto rectifyPurchaseDate(ConfirmReceiptToRectifyDto dto) {
        return rectifyProductsDate(rectifyWithManualDate(dto));
    }

    private ProductsToRectifyDto rectifyDate(ProcessReceiptToRectifyDto dto) {
        LocalDate today = LocalDate.now(dto.clock());
        LocalDate rectifiedPurchaseDate = dto.purchaseDate().isAfter(today) ? today : dto.purchaseDate();
        return new ProductsToRectifyDto(dto.purchaseDate(), rectifiedPurchaseDate, dto.productExtractions());
    }

    private ProductsToRectifyDto rectifyWithManualDate(ConfirmReceiptToRectifyDto dto) {
        LocalDate today = LocalDate.now(dto.clock());
        LocalDate rectifiedPurchaseDate = dto.editedPurchaseDate().isAfter(today)
                ? dto.oldPurchaseDate()
                : dto.editedPurchaseDate();
        return new ProductsToRectifyDto(dto.oldPurchaseDate(), rectifiedPurchaseDate, dto.productExtractions());
    }

    private RectifiedReceiptDto rectifyProductsDate(ProductsToRectifyDto dto) {
        long purchaseDateCorrectionDays = ChronoUnit.DAYS.between(
                dto.originalPurchaseDate(), dto.rectifiedPurchaseDate()
        );
        List<ProductExtraction> rectifiedProducts = dto.productExtractions().stream()
                .map(p -> withCorrectedExpirationDate(p, purchaseDateCorrectionDays))
                .toList();
        return new RectifiedReceiptDto(dto.rectifiedPurchaseDate(), rectifiedProducts);
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
