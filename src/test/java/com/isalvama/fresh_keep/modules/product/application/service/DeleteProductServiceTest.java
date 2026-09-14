package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);

    private DeleteProductService service;

    private final String userId = UserId.create().toString();
    private final StorageSpotId storageSpotId = StorageSpotId.create();

    private final Product product = Product.create(
            ProductName.from("Milk"),
            LocalDate.of(2026, 9, 20),
            storageSpotId,
            ProductType.DAIRY,
            ShoppingReceiptId.create(),
            Money.from(BigDecimal.valueOf(1.5), "USD")
    );

    private final DeleteProductCommand command = new DeleteProductCommand(userId, product.getId().toString());

    @BeforeEach
    void setUp() {
        service = new DeleteProductService(productRepositoryPort, spaceParticipancyLookUpPort, clock);
    }

    @Test
    void execute_throwsNonExistentProductExceptionWhenProductDoesNotExist() {
        when(productRepositoryPort.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NonExistentProductException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains(product.getId().toString()));
        verifyNoInteractions(spaceParticipancyLookUpPort);
        verify(productRepositoryPort, never()).delete(any(), any());
    }

    @Test
    void execute_throwsSpaceNotAccessibleExceptionWhenUserIsNotParticipant() {
        when(productRepositoryPort.findById(ProductId.from(command.productId()))).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.isParticipant(any(), any())).thenReturn(false);

        assertThrows(SpaceNotAccessibleException.class, () -> service.execute(command));

        verify(productRepositoryPort, never()).delete(any(), any());
    }

    @Test
    void execute_deletesProductWhenUserIsParticipant() {
        when(productRepositoryPort.findById(ProductId.from(command.productId()))).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.isParticipant(any(), any())).thenReturn(true);

        service.execute(command);

        verify(productRepositoryPort).delete(product, clock);
    }

    @Test
    void execute_passesTheProductsActualStorageSpotIdToTheParticipancyCheck() {
        when(productRepositoryPort.findById(ProductId.from(command.productId()))).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.isParticipant(any(), any())).thenReturn(true);

        service.execute(command);

        verify(spaceParticipancyLookUpPort).isParticipant(userId, storageSpotId.toString());
    }
}
