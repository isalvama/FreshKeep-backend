package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.product_shopping_date_look_up;

import com.isalvama.fresh_keep.modules.product.application.port.out.ProductShoppingDateLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ShoppingReceiptRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ProductShoppingDateLookUpAdapter implements ProductShoppingDateLookUpPort {
    private final ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;

    @Override
    public LocalDate findShoppingDate(String shoppingReceiptId) {
        return shoppingReceiptRepositoryPort.getShoppingDate(ShoppingReceiptId.from(shoppingReceiptId));
    }
}
