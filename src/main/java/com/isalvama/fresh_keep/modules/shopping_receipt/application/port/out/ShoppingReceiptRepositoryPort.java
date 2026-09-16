package com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;

import java.time.LocalDate;
import java.util.Optional;

public interface ShoppingReceiptRepositoryPort {
    void save (ShoppingReceipt shoppingReceipt);
    Optional<LocalDate> getShoppingDate (ShoppingReceiptId shoppingReceiptId);
}
