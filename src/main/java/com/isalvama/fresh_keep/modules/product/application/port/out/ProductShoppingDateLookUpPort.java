package com.isalvama.fresh_keep.modules.product.application.port.out;

import java.time.LocalDate;

public interface ProductShoppingDateLookUpPort {
    LocalDate findShoppingDate(String shoppingReceiptId);
}
