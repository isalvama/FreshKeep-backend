package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetShoppingReceiptsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.DailyReceiptSummaryResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptDetailResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptSummaryResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentShoppingReceiptException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetShoppingReceiptsService implements GetShoppingReceiptsUseCase {
    private final ShoppingReceiptQueryPort receiptQueryPort;

    @Override
    @Transactional(readOnly = true)
    public List<DailyReceiptSummaryResult> getShoppingReceiptMetrics(GetShoppingReceiptMetricsCommand command) {
        return receiptQueryPort.getShoppingReceiptMetrics(command.from(), command.to(), command.spaceId(), command.creatorId()).stream()
                .map(summary -> new DailyReceiptSummaryResult(summary.date(), summary.totalReceipts()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReceiptSummaryResult> getShoppingReceipts(GetShoppingReceiptsCommand command) {
        return receiptQueryPort.findReceipts(command.from(), command.to(), command.spaceId(), command.userId()).stream()
                .map(receipt -> new ReceiptSummaryResult(
                        receipt.id(), receipt.creatorId(), receipt.spaceId(), receipt.storeName(),
                        receipt.purchaseDate(), receipt.createdAt()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDetailResult getShoppingReceipt(UUID receiptId) {
        ReceiptDetailDto receipt = receiptQueryPort.findReceiptById(receiptId)
                .orElseThrow(() -> new NonExistentShoppingReceiptException(
                        "Shopping receipt with id " + receiptId + " does not exist."));
        return new ReceiptDetailResult(
                receipt.id(), receipt.creatorId(), receipt.creatorUsername(), receipt.creatorEmail(),
                receipt.spaceId(), receipt.spaceName(), receipt.storeName(), receipt.purchaseDate(), receipt.createdAt(),
                receipt.receiptImageId(), receipt.receiptImageAssetId(), receipt.receiptImageMimeType(),
                receipt.products().stream()
                        .map(product -> new ReceiptDetailResult.ProductResult(
                                product.id(), product.name(), product.expirationDate(), product.actualStorageSpotId(),
                                product.productType(), product.price(), product.currency()))
                        .toList());
    }
}
