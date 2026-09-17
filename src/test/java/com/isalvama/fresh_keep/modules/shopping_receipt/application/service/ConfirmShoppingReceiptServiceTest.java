package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductRegistrationPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ShoppingReceiptRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceiptStatus;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmShoppingReceiptServiceTest {

    @Mock
    private SpaceLookUpPort spaceLookUpPort;
    @Mock
    private ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    @Mock
    private StorageSpotSuggestionResolver storageSpotSuggestionResolver;
    @Mock
    private ExtractionDataRectifier extractionDataRectifier;
    @Mock
    private ProductRegistrationPort productRegistrationPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-05T10:00:00Z"), ZoneOffset.UTC);

    private ConfirmShoppingReceiptService service;

    private final String receiptImageId = ReceiptImageId.create().toString();
    private final String shoppingReceiptId = ShoppingReceiptId.create().toString();
    private final String spaceId = SpaceId.create().toString();
    private final String creatorId = UserId.create().toString();
    private final LocalDate shoppingDate = LocalDate.of(2026, 9, 5);
    private final String storeName = "SuperMart";

    private final ProductCommand productCommand = new ProductCommand(
            LocalDate.of(2026, 9, 15), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD", false);

    private final ConfirmShoppingReceiptCommand command = new ConfirmShoppingReceiptCommand(
                shoppingReceiptId, receiptImageId, spaceId, creatorId, shoppingDate, storeName, List.of(productCommand)
    );

    private final List<StorageSpotDto> storageSpots = List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"));

    private final List<ProductExtraction> resolvedProductExtractions = List.of(
            new ProductExtraction(LocalDate.of(2026, 9, 15), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")
    );

    private final List<RegisteredProductDto> registeredProducts = List.of(
            new RegisteredProductDto(UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 15), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")
    );

    private final ShoppingReceipt shoppingReceipt = ShoppingReceipt.reconstitute(
            ShoppingReceiptId.from(shoppingReceiptId),
            UserId.from(creatorId),
            SpaceId.from(spaceId),
            ReceiptImageId.from(receiptImageId),
            shoppingDate,
            storeName,
            ShoppingReceiptStatus.DRAFT
    );

    @BeforeEach
    void setUp() {
        service = new ConfirmShoppingReceiptService(
                spaceLookUpPort,
                shoppingReceiptRepositoryPort,
                storageSpotSuggestionResolver,
                extractionDataRectifier,
                productRegistrationPort,
                clock
        );
    }

    private void stubHappyPath() {
        when(spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(any())).thenReturn(storageSpots);
        when(shoppingReceiptRepositoryPort.getById(ShoppingReceiptId.from(shoppingReceiptId))).thenReturn(Optional.of(shoppingReceipt));
        when(storageSpotSuggestionResolver.resolve(any(), any())).thenReturn(resolvedProductExtractions);
        when(productRegistrationPort.registerProducts(any())).thenReturn(registeredProducts);
    }

    @Test
    void execute_throwsNonExistentShoppingReceiptExceptionWhenReceiptDoesNotExist() {
        when(spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(any())).thenReturn(storageSpots);
        when(shoppingReceiptRepositoryPort.getById(ShoppingReceiptId.from(shoppingReceiptId))).thenReturn(Optional.empty());

        assertThrows(NonExistentShoppingReceiptException.class, () -> service.execute(command));

        verifyNoInteractions(storageSpotSuggestionResolver, productRegistrationPort);
    }

    @Test
    void execute_returnsResultAssembledFromRegisteredProductsAndStorageSpots() {
        stubHappyPath();

        ArgumentCaptor<ShoppingReceipt> shoppingReceiptCaptor = ArgumentCaptor.forClass(ShoppingReceipt.class);

        ShoppingReceiptResult result = service.execute(command);

        verify(shoppingReceiptRepositoryPort).update(shoppingReceiptCaptor.capture());
        String expectedShoppingReceiptId = shoppingReceiptCaptor.getValue().getId().toString();

        assertEquals(expectedShoppingReceiptId, result.shoppingReceiptId());
        assertEquals(shoppingDate, result.shoppingDate());
        assertEquals(storeName, result.storeName());

        assertEquals(1, result.products().size());
        assertEquals("Milk", result.products().getFirst().productName());
        assertEquals(registeredProducts.getFirst().id().toString(), result.products().getFirst().id());

        assertEquals(1, result.storageSpots().size());
        assertEquals("fridge-id", result.storageSpots().getFirst().id());
        assertEquals("Fridge", result.storageSpots().getFirst().name());
        assertEquals("FRIDGE", result.storageSpots().getFirst().type());
    }

    @Test
    void execute_passesTheExpectedArgumentsToEachCollaborator() {
        stubHappyPath();

        service.execute(command);

        verify(spaceLookUpPort).getStorageSpotsBySpaceIdAndParticipantId(GetStorageSpotsDto.create(spaceId, creatorId));
        verify(storageSpotSuggestionResolver).resolve(List.of(
                new ProductExtraction(LocalDate.of(2026, 9, 15), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")
        ), storageSpots);

        ArgumentCaptor<List<RegisterProductDto>> registerDtosCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRegistrationPort).registerProducts(registerDtosCaptor.capture());
        RegisterProductDto registeredDto = registerDtosCaptor.getValue().getFirst();
        assertEquals("Milk", registeredDto.productName());
        assertEquals(LocalDate.of(2026, 9, 15), registeredDto.expirationDate());
        assertEquals(UUID.fromString(creatorId), registeredDto.creatorId());
        assertEquals("fridge-id", registeredDto.suggestedStorageSpotId());
        assertEquals("DAIRY", registeredDto.productType());
        assertEquals(BigDecimal.valueOf(1.5), registeredDto.priceAmount());
        assertEquals("USD", registeredDto.currency());
    }

    @Test
    void execute_persistsAShoppingReceiptWithTheCommandsShoppingDateAndStoreNameUnchanged() {
        stubHappyPath();

        service.execute(command);

        ArgumentCaptor<ShoppingReceipt> shoppingReceiptCaptor = ArgumentCaptor.forClass(ShoppingReceipt.class);
        verify(shoppingReceiptRepositoryPort).update(shoppingReceiptCaptor.capture());

        ShoppingReceipt savedReceipt = shoppingReceiptCaptor.getValue();
        assertEquals(UserId.from(creatorId), savedReceipt.getCreatorId());
        assertEquals(SpaceId.from(spaceId), savedReceipt.getSpaceId());
        assertEquals(ReceiptImageId.from(receiptImageId), savedReceipt.getReceiptImageId());
        assertEquals(shoppingDate, savedReceipt.getPurchaseDate());
        assertEquals(storeName, savedReceipt.getStoreName());
    }

    @Test
    void execute_returnsProductsSortedByExpirationDateAscendingWithNullsLast() {
        stubHappyPath();

        List<RegisteredProductDto> unsortedRegisteredProducts = List.of(
                new RegisteredProductDto(UUID.randomUUID(), "Yogurt", LocalDate.of(2026, 9, 20), "fridge-id", "DAIRY", BigDecimal.valueOf(2.0), "USD"),
                new RegisteredProductDto(UUID.randomUUID(), "Bread", null, "fridge-id", "BAKERY", BigDecimal.valueOf(1.0), "USD"),
                new RegisteredProductDto(UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 10), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")
        );
        when(productRegistrationPort.registerProducts(any())).thenReturn(unsortedRegisteredProducts);

        ShoppingReceiptResult result = service.execute(command);

        assertEquals(3, result.products().size());
        assertEquals("Milk", result.products().get(0).productName());
        assertEquals("Yogurt", result.products().get(1).productName());
        assertEquals("Bread", result.products().get(2).productName());
    }
}
