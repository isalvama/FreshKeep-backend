package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;

public interface ReceiptImageRepositoryPort {
    void save(ReceiptImage receiptImage);
}
