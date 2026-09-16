package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.exception.MoveProductDataUnavailableException;
import com.isalvama.fresh_keep.modules.product.application.port.in.MoveProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.MoveProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.*;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductChangesDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductMoveException;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.StorageSpotId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MoveProductService implements MoveProductUseCase {
    private final ProductRepositoryPort productRepositoryPort;
    private final SpaceParticipancyLookUpPort spaceParticipancyLookUpPort;
    private final ProductShoppingDateLookUpPort productShoppingDateLookUpPort;
    private final ProductStorageSpotHistoryRepositoryPort productStorageSpotHistoryRepositoryPort;
    private final StorageSpotLookUpPort storageSpotLookUpPort;
    private final Clock clock;
    private final ProductMovedExpirationDateCalculatorPort productMovedExpirationDateCalculatorPort;

    @Override
    public MoveProductResult execute(MoveProductCommand command) {

        Product product = productRepositoryPort.findById(ProductId.from(command.productId().toString()))
                .orElseThrow(() -> new NonExistentProductException("Product with id " + command.productId() + " does not exist."));

        if (!product.getActualStorageSpotId().value()
                .equals(command.oldStorageSpotId())) {
            throw new InvalidProductMoveException("Product cannot be moved to the same storage spot it is already stored in.");
        }

        Set<String> storageSpotIds = Set.of(command.oldStorageSpotId().toString(), command.newStorageSpotId().toString());
        Set<String> accessibleSpotsIds = spaceParticipancyLookUpPort.filterAccessible(command.userId().toString(), storageSpotIds);
        List<String> inaccessibleStorageSpots = storageSpotIds.stream().filter(ssId-> !accessibleSpotsIds.contains(ssId)).toList();
        if (!inaccessibleStorageSpots.isEmpty()){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the space/s of the storage spot/s with id " + String.join(", ", inaccessibleStorageSpots));
        }

        LocalDate shoppingDate = productShoppingDateLookUpPort.findShoppingDate(product.getShoppingReceiptId().toString())
                .orElseThrow(() -> new MoveProductDataUnavailableException("Unable to find the shopping date of the product to be moved, with id " + product.getId().toString()));

        List<ProductMove> productMoves = productStorageSpotHistoryRepositoryPort.findByProductId(product.getId());
        if (productMoves.isEmpty()){
            throw new MoveProductDataUnavailableException("Unable to retrieve the storage spot history of the product with id " + product.getId().toString());
        }

        Set<String> referencedStorageSpotIds = Stream.concat(
                        storageSpotIds.stream(),
                        productMoves.stream().map(ProductMove::newStorageSpotId))
                .collect(Collectors.toSet());
        Map<String, StorageSpotInfoDto> storageSpotInfoById = storageSpotLookUpPort.findByIds(referencedStorageSpotIds).stream()
                .collect(Collectors.toMap(StorageSpotInfoDto::id, Function.identity()));
       List<String> referencedStorageSpotIdsWithoutInfo = referencedStorageSpotIds.stream().filter(rss -> !storageSpotInfoById.containsKey(rss)).toList();
       if (!referencedStorageSpotIdsWithoutInfo.isEmpty()){
           throw new MoveProductDataUnavailableException("Unable to retrieve data of the storage spots with id "+ String.join(", ", referencedStorageSpotIdsWithoutInfo));
       }

        ProductMovedDto productMovedDto = new ProductMovedDto(
                product.getName().value(),
                product.getProductType().name(),
                shoppingDate,
                productMoves.stream().map(pm -> ProductChangesDto.from(pm, storageSpotInfoById.get(pm.newStorageSpotId()))).toList(),
                storageSpotInfoById.get(command.oldStorageSpotId().toString()),
                storageSpotInfoById.get(command.newStorageSpotId().toString()),
                clock
        );

        LocalDate newExpirationDate = productMovedExpirationDateCalculatorPort.execute(productMovedDto);

        product.updateStorageSpot(StorageSpotId.from(command.newStorageSpotId().toString()), newExpirationDate);

        productRepositoryPort.save(product);

        return new MoveProductResult(
                product.getId().toString(),
                product.getActualStorageSpotId().toString(),
                product.getExpirationDate()
        );
    }
}
