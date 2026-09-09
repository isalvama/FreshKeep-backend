package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;

public interface ShoppingReceiptRepositoryPort {
    void save (ShoppingReceipt shoppingReceipt);
}
