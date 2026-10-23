package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;

public interface ReProcessShoppingReceiptUseCase {
    ShoppingReceiptResult execute (ReProcessShoppingReceiptCommand command);
}
