package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.MoveProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.*;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.application.exception.MoveProductDataUnavailableException;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductMoveException;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MoveProductServiceTest {

    @Mock private ProductRepositoryPort productRepositoryPort;
    @Mock private SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;
    @Mock private ProductShoppingDateLookUpPort productShoppingDateLookUpPort;
    @Mock private ProductStorageSpotHistoryRepositoryPort historyRepositoryPort;
    @Mock private StorageSpotLookUpPort storageSpotLookUpPort;
    @Mock private ProductMovedExpirationDateCalculatorPort expirationDateCalculatorPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-16T10:00:00Z"), ZoneOffset.UTC);
    private final UUID userId = UUID.randomUUID();
    private final StorageSpotId oldStorageSpotId = StorageSpotId.create();
    private final StorageSpotId newStorageSpotId = StorageSpotId.create();
    private final Product product = Product.create(
            ProductName.from("Milk"), LocalDate.of(2026, 9, 20), oldStorageSpotId,
            ProductType.DAIRY, ShoppingReceiptId.create(), Money.from(BigDecimal.valueOf(1.5), "USD"));
    private final ProductMove initialMove = new ProductMove(
            userId.toString(), oldStorageSpotId.toString(),
            LocalDateTime.of(2026, 9, 2, 10, 0), LocalDate.of(2026, 9, 20));
    private MoveProductService service;

    @BeforeEach
    void setUp() {
        service = new MoveProductService(productRepositoryPort, spaceParticipancyLookUpPort,
                productShoppingDateLookUpPort, historyRepositoryPort, storageSpotLookUpPort,
                clock, expirationDateCalculatorPort);
    }

    @Test
    void execute_movesTheProductAndReturnsTheUpdatedValues() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        LocalDate newExpirationDate = LocalDate.of(2026, 9, 25);
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.filterAccessible(eq(userId.toString()), any()))
                .thenReturn(Set.of(oldStorageSpotId.toString(), newStorageSpotId.toString()));
        when(productShoppingDateLookUpPort.findShoppingDate(product.getShoppingReceiptId().toString()))
                .thenReturn(Optional.of(LocalDate.of(2026, 9, 1)));
        when(historyRepositoryPort.findByProductId(product.getId())).thenReturn(List.of(initialMove));
        when(storageSpotLookUpPort.findByIds(any())).thenReturn(List.of(
                StorageSpotInfoDto.create(oldStorageSpotId.toString(), "Fridge", "FRIDGE"),
                StorageSpotInfoDto.create(newStorageSpotId.toString(), "Freezer", "FREEZER")));
        when(expirationDateCalculatorPort.execute(any(ProductMovedDto.class))).thenReturn(newExpirationDate);

        MoveProductResult result = service.execute(command);

        assertEquals(product.getId().toString(), result.productId());
        assertEquals(newStorageSpotId.toString(), result.newStorageSpotId());
        assertEquals(newExpirationDate, result.newExpirationDate());
        verify(productRepositoryPort).save(product);
        assertEquals(newStorageSpotId, product.getActualStorageSpotId());
        assertEquals(newExpirationDate, product.getExpirationDate());
    }

    @Test
    void execute_providesProductContextToTheExpirationDateCalculator() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.filterAccessible(any(), any())).thenReturn(
                Set.of(oldStorageSpotId.toString(), newStorageSpotId.toString()));
        when(productShoppingDateLookUpPort.findShoppingDate(any())).thenReturn(Optional.of(LocalDate.of(2026, 9, 1)));
        when(historyRepositoryPort.findByProductId(any())).thenReturn(List.of(initialMove));
        when(storageSpotLookUpPort.findByIds(any())).thenReturn(List.of(
                StorageSpotInfoDto.create(oldStorageSpotId.toString(), "Fridge", "FRIDGE"),
                StorageSpotInfoDto.create(newStorageSpotId.toString(), "Freezer", "FREEZER")));
        when(expirationDateCalculatorPort.execute(any())).thenReturn(LocalDate.of(2026, 9, 25));

        service.execute(command);

        ArgumentCaptor<ProductMovedDto> captor = ArgumentCaptor.forClass(ProductMovedDto.class);
        verify(expirationDateCalculatorPort).execute(captor.capture());
        ProductMovedDto moved = captor.getValue();
        assertEquals("Milk", moved.productName());
        assertEquals("DAIRY", moved.productType());
        assertEquals(oldStorageSpotId.toString(), moved.oldStorageSpotInfo().id());
        assertEquals(newStorageSpotId.toString(), moved.newStorageSpotInfo().id());
        assertEquals(1, moved.productChanges().size());
        assertEquals("Fridge", moved.productChanges().getFirst().newStorageSpotName());
        assertSame(clock, moved.clock());
    }

    @Test
    void execute_throwsWhenProductDoesNotExist() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        when(productRepositoryPort.findById(any())).thenReturn(Optional.empty());

        assertThrows(NonExistentProductException.class, () -> service.execute(command));

        verifyNoInteractions(spaceParticipancyLookUpPort, expirationDateCalculatorPort);
    }

    @Test
    void execute_throwsWhenEitherStorageSpotIsNotAccessible() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.filterAccessible(any(), any())).thenReturn(Set.of(oldStorageSpotId.toString()));

        assertThrows(SpaceNotAccessibleException.class, () -> service.execute(command));

        verifyNoInteractions(productShoppingDateLookUpPort, historyRepositoryPort,
                storageSpotLookUpPort, expirationDateCalculatorPort);
        verify(productRepositoryPort, never()).save(any());
    }

    @Test
    void execute_throwsWhenOldStorageSpotDoesNotMatchTheProductLocation() {
        StorageSpotId differentOldStorageSpotId = StorageSpotId.create();
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                differentOldStorageSpotId.value(), newStorageSpotId.value());
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));

        assertThrows(InvalidProductMoveException.class, () -> service.execute(command));

        verifyNoInteractions(spaceParticipancyLookUpPort, productShoppingDateLookUpPort,
                historyRepositoryPort, storageSpotLookUpPort, expirationDateCalculatorPort);
    }

    @Test
    void execute_throwsWhenShoppingDateIsUnavailable() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        givenProductIsAccessible(command);
        when(productShoppingDateLookUpPort.findShoppingDate(any())).thenReturn(Optional.empty());

        assertThrows(MoveProductDataUnavailableException.class, () -> service.execute(command));

        verifyNoInteractions(historyRepositoryPort, storageSpotLookUpPort, expirationDateCalculatorPort);
    }

    @Test
    void execute_throwsWhenProductHistoryIsEmpty() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        givenProductIsAccessible(command);
        when(productShoppingDateLookUpPort.findShoppingDate(any())).thenReturn(Optional.of(LocalDate.of(2026, 9, 1)));
        when(historyRepositoryPort.findByProductId(any())).thenReturn(List.of());

        assertThrows(MoveProductDataUnavailableException.class, () -> service.execute(command));

        verifyNoInteractions(storageSpotLookUpPort, expirationDateCalculatorPort);
    }

    @Test
    void execute_throwsWhenStorageSpotInformationIsIncomplete() {
        MoveProductCommand command = new MoveProductCommand(userId, product.getId().value(),
                oldStorageSpotId.value(), newStorageSpotId.value());
        givenProductIsAccessible(command);
        when(productShoppingDateLookUpPort.findShoppingDate(any())).thenReturn(Optional.of(LocalDate.of(2026, 9, 1)));
        when(historyRepositoryPort.findByProductId(any())).thenReturn(List.of(initialMove));
        when(storageSpotLookUpPort.findByIds(any())).thenReturn(List.of(
                StorageSpotInfoDto.create(oldStorageSpotId.toString(), "Fridge", "FRIDGE")));

        assertThrows(MoveProductDataUnavailableException.class, () -> service.execute(command));

        verifyNoInteractions(expirationDateCalculatorPort);
    }

    private void givenProductIsAccessible(MoveProductCommand command) {
        when(productRepositoryPort.findById(any())).thenReturn(Optional.of(product));
        when(spaceParticipancyLookUpPort.filterAccessible(command.userId().toString(), Set.of(
                command.oldStorageSpotId().toString(), command.newStorageSpotId().toString())))
                .thenReturn(Set.of(command.oldStorageSpotId().toString(), command.newStorageSpotId().toString()));
    }
}
