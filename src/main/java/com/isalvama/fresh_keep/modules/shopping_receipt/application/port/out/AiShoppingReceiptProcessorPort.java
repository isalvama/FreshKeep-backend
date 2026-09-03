package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ProcessNewShoppingReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.ReceiptExtraction;

public interface AiShoppingReceiptProcessorPort {
    ReceiptExtraction process(ProcessNewShoppingReceiptDto processNewShoppingReceiptDto);
}
