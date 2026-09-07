package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ReProcessShoppingReceiptResult;

public interface ReProcessShoppingReceiptUseCase {
    ReProcessShoppingReceiptResult execute (ReProcessShoppingReceiptCommand command);
}
