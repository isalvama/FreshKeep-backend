package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidSpaceReferenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetSpaceOverviewCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.GetSpaceOverviewResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceProductsLookUpPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.dto.SpaceProductDto;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpot;
import com.isalvama.fresh_keep.modules.space.domain.model.StorageSpotType;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.Emoji;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceName;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotName;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetSpaceOverviewServiceTest {

    @Mock
    private SpaceRepositoryPort spaceRepositoryPort;
    @Mock
    private SpaceProductsLookUpPort spaceProductsLookUpPort;

    @InjectMocks
    private GetSpaceOverviewService service;

    private final UserId userId = UserId.create();
    private final StorageSpot fridge = StorageSpot.create(StorageSpotName.from("Fridge"), StorageSpotType.FRIDGE);
    private final StorageSpot pantry = StorageSpot.create(StorageSpotName.from("Pantry"), StorageSpotType.PANTRY);
    private final Space space = Space.create(SpaceName.from("Kitchen"), Emoji.from("😀"), Set.of(fridge, pantry), userId);
    private final GetSpaceOverviewCommand command = new GetSpaceOverviewCommand(space.getId().toString(), userId.toString());

    private final List<SpaceProductDto> products = List.of(
            new SpaceProductDto(
                    UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 20),
                    UUID.fromString(fridge.getId().toString()), "DAIRY",
                    UUID.randomUUID(), BigDecimal.valueOf(1.5), "USD")
    );

    @Test
    void execute_throwsInvalidSpaceReferenceExceptionWhenSpaceDoesNotExist() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(InvalidSpaceReferenceException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains("does not exist"));
        verify(spaceRepositoryPort, never()).getByParticipantId(any());
        verifyNoInteractions(spaceProductsLookUpPort);
    }

    @Test
    void execute_throwsSpaceNotAccessibleExceptionWhenUserIsNotAParticipant() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of());

        Exception exception = assertThrows(SpaceNotAccessibleException.class, () -> service.execute(command));

        assertTrue(exception.getMessage().contains("is not a participant"));
        verifyNoInteractions(spaceProductsLookUpPort);
    }

    @Test
    void execute_returnsResultAssembledFromSpaceStorageSpotsAndProducts() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of(space));
        when(spaceProductsLookUpPort.getProductsFromSpaceId(any())).thenReturn(products);

        GetSpaceOverviewResult result = service.execute(command);

        assertEquals(space.getId().toString(), result.id());
        assertEquals("Kitchen", result.name());
        assertEquals("😀", result.emoji());

        assertEquals(2, result.storageSpots().size());
        assertTrue(result.storageSpots().stream().anyMatch(s ->
                s.id().equals(fridge.getId().toString()) && s.name().equals("Fridge") && s.type().equals("FRIDGE")));
        assertTrue(result.storageSpots().stream().anyMatch(s ->
                s.id().equals(pantry.getId().toString()) && s.name().equals("Pantry") && s.type().equals("PANTRY")));

        assertEquals(1, result.productResults().size());
        assertEquals(products.getFirst().id().toString(), result.productResults().getFirst().id());
        assertEquals("Milk", result.productResults().getFirst().productName());
        assertEquals(LocalDate.of(2026, 9, 20), result.productResults().getFirst().expirationDate());
        assertEquals(fridge.getId().toString(), result.productResults().getFirst().storageSpotId());
        assertEquals("DAIRY", result.productResults().getFirst().productType());
        assertEquals(BigDecimal.valueOf(1.5), result.productResults().getFirst().priceAmount());
        assertEquals("USD", result.productResults().getFirst().currency());
    }

    @Test
    void execute_passesTheSpaceIdAsUuidToTheProductsLookUpPort() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of(space));
        when(spaceProductsLookUpPort.getProductsFromSpaceId(any())).thenReturn(List.of());

        service.execute(command);

        ArgumentCaptor<UUID> captor = ArgumentCaptor.forClass(UUID.class);
        verify(spaceProductsLookUpPort).getProductsFromSpaceId(captor.capture());
        assertEquals(UUID.fromString(space.getId().toString()), captor.getValue());
    }

    @Test
    void execute_returnsEmptyProductResultsWhenSpaceHasNoProducts() {
        when(spaceRepositoryPort.getById(any())).thenReturn(Optional.of(space));
        when(spaceRepositoryPort.getByParticipantId(any())).thenReturn(List.of(space));
        when(spaceProductsLookUpPort.getProductsFromSpaceId(any())).thenReturn(List.of());

        GetSpaceOverviewResult result = service.execute(command);

        assertTrue(result.productResults().isEmpty());
    }
}
