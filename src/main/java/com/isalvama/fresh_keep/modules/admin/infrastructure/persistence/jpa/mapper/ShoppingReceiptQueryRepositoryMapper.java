package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.persistence.jpa.projection.ReceiptSummaryProjection;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ShoppingReceiptQueryRepositoryMapper {

    public ReceiptSummaryDto toSummaryDto(ReceiptSummaryProjection row) {
        return new ReceiptSummaryDto(
                row.id(),
                row.creatorId(),
                row.creatorEmail(),
                row.creatorUsername(),
                row.spaceId(),
                row.spaceName(),
                row.storeName(),
                row.purchaseDate().atZone(ZoneOffset.UTC).toLocalDate(),
                row.createdAt(),
                row.productCount());
    }

    public ReceiptDetailDto toDetailDto(ReceiptDetailProjection header, List<ReceiptProductDto> products) {
        return new ReceiptDetailDto(
                header.id(), header.creatorId(), header.creatorEmail(), header.creatorUsername(),
                header.spaceId(), header.spaceName(), header.storeName(),
                header.purchaseDate().atZone(ZoneOffset.UTC).toLocalDate(),
                header.createdAt(), header.receiptImageId(), header.receiptImageAssetId(),
                header.receiptImageMimeType(), products);
    }
}
