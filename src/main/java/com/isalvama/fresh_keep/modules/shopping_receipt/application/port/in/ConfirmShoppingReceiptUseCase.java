package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;

public interface ConfirmShoppingReceiptUseCase {
    ShoppingReceiptResult execute (ConfirmShoppingReceiptCommand command);
}
