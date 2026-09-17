package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifyExtractionDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidReceiptImageException;
import com.isalvama.fresh_keep.shared.infrastructure.exception.InfrastructureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessNewShoppingReceiptServiceTest {

    @Mock
    private SpaceLookUpPort spaceLookUpPort;
    @Mock
    private ProductCategoriesLookUpPort productCategoriesLookUpPort;
    @Mock
    private AiShoppingReceiptProcessorPort shoppingReceiptProcessorPort;
    @Mock
    private AiReceiptExtractionReviewerPort extractionReviewerPort;
    @Mock
    private ImageStoragePort imageStoragePort;
    @Mock
    private ReceiptImageRepositoryPort receiptImageRepositoryPort;
    @Mock
    private ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    @Mock
    private ExtractionDataRectifier extractionDataRectifier;
    @Mock
    private StorageSpotSuggestionResolver spotSuggestionResolver;
    @Mock
    private LanguageResolver languageResolver;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-01T10:00:00Z"), ZoneOffset.UTC);

    private ProcessNewShoppingReceiptService service;

    private final MultipartFile file = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", "fake-image-content".getBytes());
    private final String creatorId = "11111111-1111-1111-1111-111111111111";
    private final String spaceId = "22222222-2222-2222-2222-222222222222";
    private final String language = "es";
    private final String resolvedLanguage = "Spanish";
    private final ProcessNewShoppingReceiptCommand command = new ProcessNewShoppingReceiptCommand(file, creatorId, spaceId, language);

    private final List<StorageSpotDto> storageSpots = List.of(StorageSpotDto.create("fridge-id", "Fridge", "FRIDGE"));
    private final CategoriesDto categories = new CategoriesDto(List.of("DAIRY"), List.of("USD"));

    private final ReceiptExtraction rawExtraction = new ReceiptExtraction(
            LocalDate.of(2026, 9, 2), "SuperMart", null,
            List.of(new ProductExtraction(LocalDate.of(2026, 9, 10), "Milk-Raw", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD"))
    );

    private final ReceiptExtraction dateRectifiedExtraction = new ReceiptExtraction(
            LocalDate.of(2026, 9, 1), "SuperMart", null,
            List.of(new ProductExtraction(LocalDate.of(2026, 9, 9), "Milk-DateRectified", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD"))
    );

    private final List<ProductExtraction> resolvedProductExtractions = List.of(
            new ProductExtraction(LocalDate.of(2026, 9, 9), "Milk-Resolved", "fridge-id", "DAIRY", BigDecimal.valueOf(2.5), "USD")
    );

    @BeforeEach
    void setUp() {
        service = new ProcessNewShoppingReceiptService(
                spaceLookUpPort,
                productCategoriesLookUpPort,
                languageResolver,
                shoppingReceiptProcessorPort,
                extractionReviewerPort,
                imageStoragePort,
                receiptImageRepositoryPort,
                extractionDataRectifier,
                spotSuggestionResolver,
                shoppingReceiptRepositoryPort,
                clock
        );
    }

    private void stubHappyPath() {
        when(spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(any())).thenReturn(storageSpots);
        when(productCategoriesLookUpPort.getProductTypesAndMoneyCurrencyConstNames()).thenReturn(categories);
        when(languageResolver.resolve(any())).thenReturn(resolvedLanguage);
        when(shoppingReceiptProcessorPort.process(any())).thenReturn(rawExtraction);
        when(extractionDataRectifier.rectifyPurchaseDate(any())).thenReturn(dateRectifiedExtraction);
        when(spotSuggestionResolver.resolve(any(), any())).thenReturn(resolvedProductExtractions);
        when(extractionReviewerPort.review(any())).thenReturn(List.of());
        when(imageStoragePort.upload(any(), any())).thenReturn("shopping_receipts/receipts/abc123");
    }

    @Test
    void execute_throwsInvalidReceiptImageExceptionWhenFileIsEmpty() {
        MultipartFile emptyFile = new MockMultipartFile("file", "receipt.jpg", "image/jpeg", new byte[0]);
        ProcessNewShoppingReceiptCommand emptyFileCommand = new ProcessNewShoppingReceiptCommand(emptyFile, creatorId, spaceId, language);

        assertThrows(InvalidReceiptImageException.class, () -> service.execute(emptyFileCommand));

        verifyNoInteractions(spaceLookUpPort, productCategoriesLookUpPort, languageResolver, shoppingReceiptProcessorPort,
                extractionReviewerPort, imageStoragePort, receiptImageRepositoryPort, extractionDataRectifier, spotSuggestionResolver,
                shoppingReceiptRepositoryPort);
    }

    @Test
    void execute_returnsResultAssembledFromEachCollaborator() {
        stubHappyPath();

        ProductReviewFlag flag = new ProductReviewFlag(resolvedProductExtractions.getFirst(), "expiration date looks off");
        when(extractionReviewerPort.review(any())).thenReturn(List.of(flag));

        ArgumentCaptor<ReceiptImage> receiptImageCaptor = ArgumentCaptor.forClass(ReceiptImage.class);

        ProcessNewShoppingReceiptResult result = service.execute(command);

        verify(receiptImageRepositoryPort).save(receiptImageCaptor.capture());
        String expectedReceiptImageId = receiptImageCaptor.getValue().getId().toString();

        assertEquals(expectedReceiptImageId, result.receiptImageId());
        verify(shoppingReceiptRepositoryPort).save(any());
        assertNotNull(result.shoppingReceiptId());
        assertEquals(dateRectifiedExtraction.purchaseDate(), result.purchaseShoppingDate());
        assertEquals(dateRectifiedExtraction.storeName(), result.storeName());

        assertEquals(1, result.storageSpotResults().size());
        assertEquals("fridge-id", result.storageSpotResults().getFirst().id());
        assertEquals("Fridge", result.storageSpotResults().getFirst().name());
        assertEquals("FRIDGE", result.storageSpotResults().getFirst().type());

        assertEquals(1, result.productExtractions().size());
        assertEquals("Milk-Resolved", result.productExtractions().getFirst().productName());

        assertEquals(1, result.flaggedProducts().size());
        assertEquals("Milk-Resolved", result.flaggedProducts().getFirst().productName());
    }

    @Test
    void execute_passesTheExpectedArgumentsToEachCollaborator() {
        stubHappyPath();

        service.execute(command);

        verify(spaceLookUpPort).getStorageSpotsBySpaceIdAndParticipantId(GetStorageSpotsDto.create(spaceId, creatorId));
        verify(languageResolver).resolve(language);

        ArgumentCaptor<ProcessNewShoppingReceiptDto> processDtoCaptor = ArgumentCaptor.forClass(ProcessNewShoppingReceiptDto.class);
        verify(shoppingReceiptProcessorPort).process(processDtoCaptor.capture());
        assertSame(file, processDtoCaptor.getValue().file());
        assertEquals(storageSpots, processDtoCaptor.getValue().storageSpots());
        assertSame(clock, processDtoCaptor.getValue().clock());
        assertEquals(categories.productTypes(), processDtoCaptor.getValue().productTypes());
        assertEquals(categories.moneyCurrencies(), processDtoCaptor.getValue().moneyCurrencies());
        assertEquals(resolvedLanguage, processDtoCaptor.getValue().language());

        ArgumentCaptor<RectifyExtractionDto> rectifyDtoCaptor = ArgumentCaptor.forClass(RectifyExtractionDto.class);
        verify(extractionDataRectifier).rectifyPurchaseDate(rectifyDtoCaptor.capture());
        assertSame(rawExtraction, rectifyDtoCaptor.getValue().extraction());
        assertEquals(storageSpots, rectifyDtoCaptor.getValue().storageSpots());
        assertSame(clock, rectifyDtoCaptor.getValue().clock());

        verify(spotSuggestionResolver).resolve(dateRectifiedExtraction.productExtractions(), storageSpots);

        ArgumentCaptor<ReviewNewShoppingReceiptDto> reviewDtoCaptor = ArgumentCaptor.forClass(ReviewNewShoppingReceiptDto.class);
        verify(extractionReviewerPort).review(reviewDtoCaptor.capture());
        assertEquals(storageSpots, reviewDtoCaptor.getValue().storageSpots());
        assertEquals(resolvedProductExtractions, reviewDtoCaptor.getValue().productExtractions());
        assertEquals(dateRectifiedExtraction.purchaseDate(), reviewDtoCaptor.getValue().shoppingDate());

        verify(imageStoragePort).upload(file, "shopping_receipts/receipts");
    }

    @Test
    void execute_fallsBackToAnEmptyReviewListWhenTheReviewerThrowsAnInfrastructureException() {
        stubHappyPath();
        when(extractionReviewerPort.review(any())).thenThrow(new InfrastructureException("AI reviewer is unreachable"));

        ProcessNewShoppingReceiptResult result = service.execute(command);

        assertEquals(List.of(), result.flaggedProducts());
        verify(imageStoragePort).upload(file, "shopping_receipts/receipts");
        verify(receiptImageRepositoryPort).save(any());
        assertEquals(1, result.productExtractions().size());
    }
}
