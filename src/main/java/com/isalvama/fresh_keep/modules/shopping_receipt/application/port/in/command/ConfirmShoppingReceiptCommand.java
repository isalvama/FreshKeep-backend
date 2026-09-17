package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command;

import java.time.LocalDate;
import java.util.List;

public record ConfirmShoppingReceiptCommand(
        String shoppingReceiptId,
        String receiptImageId,
        String spaceId,
        String creatorId,
        LocalDate shoppingDate,
        String storeName,
        List<ProductCommand> products
) {
}
