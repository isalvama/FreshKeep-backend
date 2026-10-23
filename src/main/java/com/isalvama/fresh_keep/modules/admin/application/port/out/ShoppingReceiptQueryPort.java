package com.isalvama.fresh_keep.modules.admin.application.port.out;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ShoppingReceiptCreationDailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptsPageDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingReceiptQueryPort {
    List<ShoppingReceiptCreationDailyMetricDto> getShoppingReceiptMetrics(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId);

    /** Newest purchase first; [totalElements] counts every match, not just this page. */
    ReceiptsPageDto findReceipts(LocalDate from, LocalDate to, UUID spaceId, UUID creatorId, int offset, int limit);

    Optional<ReceiptDetailDto> findReceiptById(UUID receiptId);
}
