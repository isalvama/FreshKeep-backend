package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProcessNewShoppingReceiptResult;

public interface ProcessNewShoppingReceiptUseCase {
    ProcessNewShoppingReceiptResult execute (ProcessNewShoppingReceiptCommand command);
}
