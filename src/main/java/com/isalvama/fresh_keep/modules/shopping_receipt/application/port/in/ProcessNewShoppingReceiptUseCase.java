package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.command.ProcessNewShoppingReceiptCommand;

public interface ProcessNewShoppingReceiptUseCase {
    void execute (ProcessNewShoppingReceiptCommand command);
}
