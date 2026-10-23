package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.mapper;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ShoppingReceiptCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptDetailProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shared.projection.MetricProjection;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.shopping_receipt_query.projection.ReceiptSummaryProjection;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;

@Component
public class ShoppingReceiptQueryMapper {

    public List<ShoppingReceiptCreationDailyMetricDto> toShoppingReceiptCreationDailyMetricDto(List<MetricProjection> rows) {
        return rows.stream().map(row -> new ShoppingReceiptCreationDailyMetricDto(row.date(), row.totalReceipts())).toList();
    }

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
