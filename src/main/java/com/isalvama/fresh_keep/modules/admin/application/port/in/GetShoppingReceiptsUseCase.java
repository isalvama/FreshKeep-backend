package com.isalvama.fresh_keep.modules.admin.application.port.in;

import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.DailyReceiptSummaryResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptDetailResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptSummaryResult;

import java.util.List;
import java.util.UUID;

public interface GetShoppingReceiptsUseCase {

    List<DailyReceiptSummaryResult> getShoppingReceiptMetrics(GetShoppingReceiptMetricsCommand command);

    /** One page, newest purchase first. */
    PageResult<ReceiptSummaryResult> getShoppingReceipts(GetShoppingReceiptsCommand command);

    ReceiptDetailResult getShoppingReceipt(UUID receiptId);
}
