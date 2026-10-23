package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.UpdateProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.UpdateProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductPriceUpdateException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateProductServiceTest {
    @Mock private ProductRepositoryPort productRepositoryPort;
    @Mock private SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;

    private final UUID userId = UUID.randomUUID();
    private final StorageSpotId storageSpotId = StorageSpotId.create();
    private Product product;
    private UpdateProductService service;

    @BeforeEach
    void setUp() {
        product = Product.create(
                ProductName.from("Milk"), LocalDate.of(2026, 9, 20), storageSpotId,
                ProductType.DAIRY, ShoppingReceiptId.create(), Money.from(BigDecimal.valueOf(1.50), "EUR"));
        service = new UpdateProductService(productRepositoryPort, spaceParticipancyLookUpPort);
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.isParticipant(userId.toString(), storageSpotId.toString())).thenReturn(true);
    }

    @Test
    void execute_updatesAmountAndPreservesCurrencyWhenCurrencyIsOmitted() {
        UpdateProductResult result = service.execute(command(BigDecimal.valueOf(2.75), null));

        assertEquals(BigDecimal.valueOf(2.75).setScale(2), result.amount());
        assertEquals("Euro", result.currency());
        assertEquals("EUR", product.getPrice().currency().name());
        verify(productRepositoryPort).save(product);
    }

    @Test
    void execute_updatesCurrencyAndPreservesAmountWhenAmountIsOmitted() {
        UpdateProductResult result = service.execute(command(null, "USD"));

        assertEquals(BigDecimal.valueOf(1.50).setScale(2), result.amount());
        assertEquals("United States Dollar", result.currency());
        assertEquals(BigDecimal.valueOf(1.50).setScale(2), product.getPrice().amount().value());
        verify(productRepositoryPort).save(product);
    }

    @Test
    void execute_requiresBothPriceFieldsWhenProductHasNoPrice() {
        product = Product.create(
                ProductName.from("Milk"), LocalDate.of(2026, 9, 20), storageSpotId,
                ProductType.DAIRY, ShoppingReceiptId.create(), null);
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        UpdateProductCommand command = command(BigDecimal.valueOf(2.75), null);

        assertThrows(InvalidProductPriceUpdateException.class, () -> service.execute(command));
        verify(productRepositoryPort, never()).save(any());
    }

    private UpdateProductCommand command(BigDecimal amount, String currency) {
        return new UpdateProductCommand(
                userId, product.getId().value(), null, null, null, amount, currency);
    }
}
