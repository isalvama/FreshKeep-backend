package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;

import java.time.LocalDate;

public interface ShoppingReceiptRepositoryPort {
    void save (ShoppingReceipt shoppingReceipt);
    LocalDate getShoppingDate (ShoppingReceiptId shoppingReceiptId);
}
