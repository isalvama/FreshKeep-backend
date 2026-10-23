package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;

import java.util.Optional;

public interface ReceiptImageRepositoryPort {
    void save(ReceiptImage receiptImage);
    Optional<ReceiptImage> findById(ReceiptImageId id);
}
