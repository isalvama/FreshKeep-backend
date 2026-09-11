package com.isalvama.fresh_keep.modules.space.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidSpaceReferenceException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.application.port.in.GetSpaceOverviewUseCase;
import com.isalvama.fresh_keep.modules.space.application.port.in.command.GetSpaceOverviewCommand;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.GetSpaceOverviewResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.SpaceProductResult;
import com.isalvama.fresh_keep.modules.space.application.port.in.dto.StorageSpotResult;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceProductsLookUpPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.SpaceRepositoryPort;
import com.isalvama.fresh_keep.modules.space.application.port.out.dto.SpaceProductDto;
import com.isalvama.fresh_keep.modules.space.domain.model.Space;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetSpaceOverviewService implements GetSpaceOverviewUseCase {
    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceProductsLookUpPort spaceProductsLookUpPort;

    @Override
    public GetSpaceOverviewResult execute(GetSpaceOverviewCommand command) {
        Space space = spaceRepositoryPort.getById(SpaceId.from(command.spaceId()))
                .orElseThrow(() -> new InvalidSpaceReferenceException("Space with id " + command.spaceId() + " does not exist."));

        List<Space> spaces = spaceRepositoryPort.getByParticipantId(UserId.from(command.userId()));

        if (!spaces.contains(space)){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the Space with id " + command.spaceId());
        }

        List<StorageSpotResult> storageSpots = space.getStorageSpots().stream()
                .map(s -> new StorageSpotResult(s.getId().toString(), s.getName().value(), s.getType().name()))
                .toList();

        List<SpaceProductDto> products = spaceProductsLookUpPort.getProductsFromSpaceId(UUID.fromString(command.spaceId()));

        return new GetSpaceOverviewResult(
                space.getId().toString(),
                space.getName().value(),
                space.getEmoji().value(),
                storageSpots,
                toSpaceProductResult(products)
        );
    }

    private List<SpaceProductResult> toSpaceProductResult(List<SpaceProductDto> products){
        return products.stream().map(p ->
         new SpaceProductResult(
                p.id().toString(),
                p.name(),
                p.expirationDate(),
                p.actualStorageSpotId().toString(),
                p.productType(),
                p.price(),
                p.currency()
         )).toList();
    }
}
