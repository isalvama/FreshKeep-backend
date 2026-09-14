package com.isalvama.fresh_keep.modules.product.application.service;

import com.isalvama.fresh_keep.modules.product.application.port.in.MoveProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.MoveProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.out.*;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductChangesDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductMovedDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.StorageSpotInfoDto;
import com.isalvama.fresh_keep.modules.product.application.service.dto.ProductMove;
import com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException;
import com.isalvama.fresh_keep.modules.product.domain.model.Product;
import com.isalvama.fresh_keep.modules.product.domain.model.value_object.ProductId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.SpaceNotAccessibleException;
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

        Product product = productRepositoryPort.findById(ProductId.from(command.productId()))
                .orElseThrow(() -> new NonExistentProductException("Product with id " + command.productId() + " does not exist."));

        Set<String> storageSpotIds = Set.of(command.oldStorageSpotId(), command.newStorageSpotId());
        Set<String> accessibleSpotsIds = spaceParticipancyLookUpPort.filterAccessible(command.userId(), storageSpotIds);

        List<String> notAccessibleProducts = storageSpotIds.stream().filter(ssId-> !accessibleSpotsIds.contains(ssId)).toList();
        if (!notAccessibleProducts.isEmpty()){
            throw new SpaceNotAccessibleException("User with id " + command.userId() + " is not a participant of the space/s of the storage spot/s with id " + String.join(", ", notAccessibleProducts));
        }

        LocalDate shoppingDate = productShoppingDateLookUpPort.findShoppingDate(product.getShoppingReceiptId().toString());

        List<ProductMove> productMoves = productStorageSpotHistoryRepositoryPort.findByProductId(product.getId());

        Set<String> referencedStorageSpotIds = Stream.concat(
                        storageSpotIds.stream(),
                        productMoves.stream().map(ProductMove::newStorageSpotId))
                .collect(Collectors.toSet());
        Map<String, StorageSpotInfoDto> storageSpotInfoById = storageSpotLookUpPort.findByIds(referencedStorageSpotIds).stream()
                .collect(Collectors.toMap(StorageSpotInfoDto::id, Function.identity()));


        ProductMovedDto productMovedDto = new ProductMovedDto(
                product.getName().value(),
                product.getProductType().name(),
                shoppingDate,
                productMoves.stream().map(pm -> ProductChangesDto.from(pm, storageSpotInfoById.get(pm.newStorageSpotId()))).toList(),
                clock
        );

        LocalDate newExpirationDate = productMovedExpirationDateCalculatorPort.execute(productMovedDto);

        return null;
    }
}
