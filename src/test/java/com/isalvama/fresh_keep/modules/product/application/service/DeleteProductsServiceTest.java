package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductRepositoryPort;
import com.isalvama.fresh_keep.modules.product.application.port.out.SpaceParticipancyLookUpPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.Money;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductName;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
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
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductsServiceTest {

    @Mock
    private ProductRepositoryPort productRepositoryPort;
    @Mock
    private SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);

    private DeleteProductsService service;

    private final UUID userId = UserId.create().value();
    private final StorageSpotId storageSpotId1 = StorageSpotId.create();
    private final StorageSpotId storageSpotId2 = StorageSpotId.create();

    private final Product milk = Product.create(
            ProductName.from("Milk"), LocalDate.of(2026, 9, 20), storageSpotId1,
            ProductType.DAIRY, ShoppingReceiptId.create(), Money.from(BigDecimal.valueOf(1.5), "USD")
    );
    private final Product yogurt = Product.create(
            ProductName.from("Yogurt"), LocalDate.of(2026, 9, 22), storageSpotId2,
            ProductType.DAIRY, ShoppingReceiptId.create(), Money.from(BigDecimal.valueOf(2.0), "USD")
    );

    private final DeleteProductsCommand command = new DeleteProductsCommand(
            userId, List.of(milk.getId().value(), yogurt.getId().value())
    );

    @BeforeEach
    void setUp() {
        service = new DeleteProductsService(productRepositoryPort, spaceParticipancyLookUpPort, clock);
    }

    private void stubHappyPath() {
        when(productRepositoryPort.findAllById(any())).thenReturn(List.of(milk, yogurt));
        when(spaceParticipancyLookUpPort.filterAccessible(any(), any()))
                .thenReturn(Set.of(storageSpotId1.toString(), storageSpotId2.toString()));
    }

    @Test
    void execute_throwsNonExistentProductExceptionWhenAnyRequestedIdIsMissing() {
        when(productRepositoryPort.findAllById(any())).thenReturn(List.of(milk));

        Exception exception = assertThrows(NonExistentProductException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains(yogurt.getId().toString()));
        verifyNoInteractions(spaceParticipancyLookUpPort);
        verify(productRepositoryPort, never()).deleteAll(any(), any());
    }

    @Test
    void execute_throwsSpaceNotAccessibleExceptionWhenAnyProductIsNotAccessible() {
        when(productRepositoryPort.findAllById(any())).thenReturn(List.of(milk, yogurt));
        when(spaceParticipancyLookUpPort.filterAccessible(any(), any()))
                .thenReturn(Set.of(storageSpotId1.toString()));

        Exception exception = assertThrows(SpaceNotAccessibleException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains(yogurt.getId().toString()));
        verify(productRepositoryPort, never()).deleteAll(any(), any());
    }

    @Test
    void execute_deletesAllProductsWhenAllExistAndAreAccessible() {
        stubHappyPath();

        service.execute(command);

        verify(productRepositoryPort).deleteAll(List.of(milk, yogurt), clock);
    }

    @Test
    void execute_passesActualStorageSpotIdsRatherThanProductIdsToTheParticipancyCheck() {
        stubHappyPath();

        service.execute(command);

        ArgumentCaptor<Set<String>> captor = ArgumentCaptor.forClass(Set.class);
        verify(spaceParticipancyLookUpPort).filterAccessible(eq(userId.toString()), captor.capture());

        Set<String> storageSpotIdsPassed = captor.getValue();
        assertEquals(Set.of(storageSpotId1.toString(), storageSpotId2.toString()), storageSpotIdsPassed);
        assertFalse(storageSpotIdsPassed.contains(milk.getId().toString()));
        assertFalse(storageSpotIdsPassed.contains(yogurt.getId().toString()));
    }
}
