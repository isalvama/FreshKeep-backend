package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ReProcessShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.AssetId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
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
class ReProcessShoppingReceiptServiceTest {

    @Mock
    private SpaceLookUpPort spaceLookUpPort;
    @Mock
    private ProductCategoriesLookUpPort productCategoriesLookUpPort;
    @Mock
    private ReceiptImageRepositoryPort receiptImageRepositoryPort;
    @Mock
    private ImageStoragePort imageStoragePort;
    @Mock
    private AiShoppingReceiptProcessorPort aiShoppingReceiptProcessorPort;
    @Mock
    private ExtractionDataRectifier extractionDataRectifier;
    @Mock
    private ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    @Mock
    private ProductRegistrationPort productRegistrationPort;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-05T10:00:00Z"), ZoneOffset.UTC);

    private ReProcessShoppingReceiptService service;

    private final String receiptImageId = ReceiptImageId.create().toString();
    private final String spaceId = SpaceId.create().toString();
    private final String creatorId = UserId.create().toString();
    private final LocalDate shoppingDate = LocalDate.of(2026, 9, 5);
    private final String storeName = "SuperMart";

    private final ProductCommand allProductCommand = new ProductCommand(
            LocalDate.of(2026, 9, 15), "Milk", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD");
    private final ProductCommand flaggedProductCommand = new ProductCommand(
            LocalDate.of(2026, 9, 20), "Yogurt", "fridge-id", "DAIRY", BigDecimal.valueOf(2.0), "USD");

    private final ReProcessShoppingReceiptCommand command = new ReProcessShoppingReceiptCommand(
            receiptImageId, spaceId, creatorId, shoppingDate, storeName,
            List.of(flaggedProductCommand), List.of(allProductCommand)
    );

    private final ReceiptImage receiptImage = ReceiptImage.create(AssetId.of("shopping_receipts/receipts/abc123"), "image/jpeg");

    private final List<StorageSpotDto> storageSpots = List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"));
    private final CategoriesDto categories = new CategoriesDto(List.of("DAIRY"), List.of("USD"));

    private final String imageUrl = "https://cloudinary.com/signed-url";
    private final byte[] imageBytes = "fake-image-content".getBytes();

    private final ReceiptExtraction rawExtraction = new ReceiptExtraction(
            LocalDate.of(2026, 9, 5), "SuperMart", null,
            List.of(new ProductExtraction(LocalDate.of(2026, 9, 15), "Milk-Raw", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD"))
    );

    private final ReceiptExtraction rectifiedExtraction = new ReceiptExtraction(
            LocalDate.of(2026, 9, 4), "SuperMart", null,
            List.of(new ProductExtraction(LocalDate.of(2026, 9, 14), "Milk-Rectified", "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD"))
    );

    private final List<RegisteredProductDto> registeredProducts = List.of(
            new RegisteredProductDto(UUID.randomUUID(), "Milk-Rectified", LocalDate.of(2026, 9, 14), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")
    );

    @BeforeEach
    void setUp() {
        service = new ReProcessShoppingReceiptService(
                spaceLookUpPort,
                productCategoriesLookUpPort,
                receiptImageRepositoryPort,
                imageStoragePort,
                aiShoppingReceiptProcessorPort,
                extractionDataRectifier,
                shoppingReceiptRepositoryPort,
                productRegistrationPort,
                clock
        );
    }

    private void stubHappyPath() {
        when(spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(any())).thenReturn(storageSpots);
        when(productCategoriesLookUpPort.getProductTypesAndMoneyCurrencyConstNames()).thenReturn(categories);
        when(receiptImageRepositoryPort.findById(ReceiptImageId.from(receiptImageId))).thenReturn(Optional.of(receiptImage));
        when(imageStoragePort.retrieveUrl(receiptImage.getAssetId().toString())).thenReturn(imageUrl);
        when(imageStoragePort.fetchImageBytes(imageUrl)).thenReturn(imageBytes);
        when(aiShoppingReceiptProcessorPort.reprocess(any())).thenReturn(rawExtraction);
        when(extractionDataRectifier.rectify(any())).thenReturn(rectifiedExtraction);
        when(productRegistrationPort.registerProducts(any())).thenReturn(registeredProducts);
    }

    @Test
    void execute_throwsNonExistentReceiptImageExceptionWhenReceiptImageDoesNotExist() {
        when(spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(any())).thenReturn(storageSpots);
        when(productCategoriesLookUpPort.getProductTypesAndMoneyCurrencyConstNames()).thenReturn(categories);
        when(receiptImageRepositoryPort.findById(ReceiptImageId.from(receiptImageId))).thenReturn(Optional.empty());

        assertThrows(NonExistentReceiptImageException.class, () -> service.execute(command));

        verifyNoInteractions(imageStoragePort, aiShoppingReceiptProcessorPort, extractionDataRectifier,
                shoppingReceiptRepositoryPort, productRegistrationPort);
    }

    @Test
    void execute_returnsResultAssembledFromRectifiedDataAndRegisteredProducts() {
        stubHappyPath();

        ArgumentCaptor<ShoppingReceipt> shoppingReceiptCaptor = ArgumentCaptor.forClass(ShoppingReceipt.class);

        ReProcessShoppingReceiptResult result = service.execute(command);

        verify(shoppingReceiptRepositoryPort).save(shoppingReceiptCaptor.capture());
        String expectedShoppingReceiptId = shoppingReceiptCaptor.getValue().getId().toString();

        assertEquals(expectedShoppingReceiptId, result.shoppingReceiptId());
        assertEquals(rectifiedExtraction.purchaseDate(), result.shoppingDate());
        assertEquals(rectifiedExtraction.storeName(), result.storeName());

        assertEquals(1, result.products().size());
        assertEquals("Milk-Rectified", result.products().getFirst().productName());
        assertEquals(registeredProducts.getFirst().id().toString(), result.products().getFirst().id());

        assertEquals(1, result.storageSpots().size());
        assertEquals("fridge-id", result.storageSpots().getFirst().id());
        assertEquals("Fridge", result.storageSpots().getFirst().name());
        assertEquals("FRIDGE", result.storageSpots().getFirst().type());
    }

    @Test
    void execute_registersProductsFromTheRectifiedExtractionNotTheRawOne() {
        stubHappyPath();

        service.execute(command);

        ArgumentCaptor<List<RegisterProductDto>> registerDtosCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRegistrationPort).registerProducts(registerDtosCaptor.capture());

        List<RegisterProductDto> registerDtos = registerDtosCaptor.getValue();
        assertEquals(1, registerDtos.size());
        assertEquals("Milk-Rectified", registerDtos.getFirst().productName());
        assertEquals(LocalDate.of(2026, 9, 14), registerDtos.getFirst().expirationDate());
    }

    @Test
    void execute_passesTheExpectedArgumentsToEachCollaborator() {
        stubHappyPath();

        service.execute(command);

        verify(spaceLookUpPort).getStorageSpotsBySpaceIdAndParticipantId(GetStorageSpotsDto.create(spaceId, creatorId));
        verify(imageStoragePort).retrieveUrl(receiptImage.getAssetId().toString());
        verify(imageStoragePort).fetchImageBytes(imageUrl);

        ArgumentCaptor<ReprocessShoppingReceiptWithFlaggedProducts> reprocessDtoCaptor = ArgumentCaptor.forClass(ReprocessShoppingReceiptWithFlaggedProducts.class);
        verify(aiShoppingReceiptProcessorPort).reprocess(reprocessDtoCaptor.capture());
        ReprocessShoppingReceiptWithFlaggedProducts reprocessDto = reprocessDtoCaptor.getValue();
        assertArrayEquals(imageBytes, reprocessDto.imageBytes());
        assertEquals(receiptImage.getMimeType(), reprocessDto.mimeType());
        assertEquals(shoppingDate, reprocessDto.purchaseDate());
        assertEquals(storeName, reprocessDto.storeName());
        assertEquals(1, reprocessDto.productExtractions().size());
        assertEquals("Milk", reprocessDto.productExtractions().getFirst().productName());
        assertEquals(1, reprocessDto.flaggedProductExtractions().size());
        assertEquals("Yogurt", reprocessDto.flaggedProductExtractions().getFirst().productName());
        assertEquals(storageSpots, reprocessDto.storageSpots());
        assertSame(clock, reprocessDto.clock());
        assertEquals(categories.productTypes(), reprocessDto.productTypes());
        assertEquals(categories.moneyCurrencies(), reprocessDto.moneyCurrencies());

        ArgumentCaptor<RectifyExtractionDto> rectifyDtoCaptor = ArgumentCaptor.forClass(RectifyExtractionDto.class);
        verify(extractionDataRectifier).rectify(rectifyDtoCaptor.capture());
        assertSame(rawExtraction, rectifyDtoCaptor.getValue().extraction());
        assertEquals(storageSpots, rectifyDtoCaptor.getValue().storageSpots());
        assertSame(clock, rectifyDtoCaptor.getValue().clock());

        ArgumentCaptor<List<RegisterProductDto>> registerDtosCaptor = ArgumentCaptor.forClass(List.class);
        verify(productRegistrationPort).registerProducts(registerDtosCaptor.capture());
        RegisterProductDto registeredDto = registerDtosCaptor.getValue().getFirst();
        assertEquals(UUID.fromString(creatorId), registeredDto.creatorId());
        assertEquals("fridge-id", registeredDto.suggestedStorageSpotId());
        assertEquals("DAIRY", registeredDto.productType());
        assertEquals(BigDecimal.valueOf(1.5), registeredDto.priceAmount());
        assertEquals("USD", registeredDto.currency());
    }

    @Test
    void execute_persistsAShoppingReceiptWithTheRectifiedPurchaseDateAndStoreName() {
        stubHappyPath();

        service.execute(command);

        ArgumentCaptor<ShoppingReceipt> shoppingReceiptCaptor = ArgumentCaptor.forClass(ShoppingReceipt.class);
        verify(shoppingReceiptRepositoryPort).save(shoppingReceiptCaptor.capture());

        ShoppingReceipt savedReceipt = shoppingReceiptCaptor.getValue();
        assertEquals(UserId.from(creatorId), savedReceipt.getCreatorId());
        assertEquals(SpaceId.from(spaceId), savedReceipt.getSpaceId());
        assertEquals(receiptImage.getId(), savedReceipt.getReceiptImageId());
        assertEquals(rectifiedExtraction.purchaseDate(), savedReceipt.getPurchaseDate());
        assertEquals(rectifiedExtraction.storeName(), savedReceipt.getStoreName());
    }
}
