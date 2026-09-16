package com.isalvama.fresh_keep.modules.product.application.port.out;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductShoppingDateLookUpPort {
    Optional<LocalDate> findShoppingDate(String shoppingReceiptId);
}
