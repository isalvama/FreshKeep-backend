package com.isalvama.fresh_keep.modules.admin.application.port.out;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingReceiptQueryPort {
    List<DailyReceiptSummaryDto> getShoppingReceiptMetrics(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId);

    List<ReceiptSummaryDto> findReceipts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId);

    Optional<ReceiptDetailDto> findReceiptById(UUID receiptId);
}
