package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ConfirmShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductRegistrationPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ShoppingReceiptRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.ConfirmReceiptToRectifyDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.service.dto.RectifiedReceiptDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentShoppingReceiptException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmShoppingReceiptService implements ConfirmShoppingReceiptUseCase {
    private final SpaceLookUpPort spaceLookUpPort;
    private final ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    private final StorageSpotSuggestionResolver storageSpotSuggestionResolver;
    private final ExtractionDataRectifier extractionDataRectifier;
    private final ProductRegistrationPort productRegistrationPort;
    private final Clock clock;


    @Override
    public ShoppingReceiptResult execute(ConfirmShoppingReceiptCommand command) {

        List<StorageSpotDto> storageSpotDtos = spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(
                GetStorageSpotsDto.create(command.spaceId(), command.creatorId())
        );

        ShoppingReceipt shoppingReceipt = shoppingReceiptRepositoryPort.getById(ShoppingReceiptId.from(command.shoppingReceiptId()))
                .orElseThrow(() -> new NonExistentShoppingReceiptException("Shopping Receipt with id " + command.shoppingReceiptId() + " does not exist."));

        if (!shoppingReceipt.getCreatorId().toString().equals(command.creatorId()) || !shoppingReceipt.getReceiptImageId().toString().equals(command.receiptImageId()) || !shoppingReceipt.getSpaceId().toString().equals(command.spaceId())) {
            throw new InvalidShoppingReceiptException("The shopping receipt cannot be confirmed in the requested context.");
        }

        List<ProductExtraction> productExtractions = toProductExtractions(command.products());

        if (!shoppingReceipt.getPurchaseDate().equals(command.shoppingDate())) {
            RectifiedReceiptDto rectifiedReceipt = extractionDataRectifier.rectifyPurchaseDate(
                    ConfirmReceiptToRectifyDto.from(
                            shoppingReceipt,
                            command.shoppingDate(),
                            clock,
                            command.products().stream().filter(p -> Boolean.FALSE.equals(p.manuallyEditedExpirationDate())).toList()
                    )
            );
            productExtractions = mergeRectifiedProducts(command.products(), rectifiedReceipt.productExtractions());
        }

        List<ProductExtraction> resolvedProductExtractions = storageSpotSuggestionResolver.resolve(productExtractions, storageSpotDtos);

        shoppingReceipt.confirm(command.shoppingDate(), command.storeName());
        shoppingReceiptRepositoryPort.update(shoppingReceipt);

        List<RegisteredProductDto> registeredProducts = productRegistrationPort.registerProducts(
                resolvedProductExtractions.stream().map(pe ->
                        new RegisterProductDto(
                                pe.productName(),
                                pe.expirationDate(),
                                pe.suggestedStorageSpotId(),
                                pe.productType(),
                                shoppingReceipt.getId().toString(),
                                pe.priceAmount(),
                                pe.currency(),
                                UUID.fromString(command.creatorId())
                        )).toList()
        );

        List<RegisteredProductDto> registeredSortedProducts = registeredProducts.stream().sorted(Comparator.comparing(RegisteredProductDto::expirationDate, Comparator.nullsLast(Comparator.naturalOrder()))).toList();

        return new ShoppingReceiptResult(
                shoppingReceipt.getId().toString(),
                shoppingReceipt.getPurchaseDate(),
                shoppingReceipt.getStoreName(),
                registeredSortedProducts.stream().map(ProductResult::toResult).toList(),
                storageSpotDtos.stream().map(SuggestedStorageSpotResult::fromStorageSpotDto).toList()
        );
    }




    private ArrayList<ProductExtraction> toProductExtractions(List<ProductCommand> products) {
        return products.stream().map(p -> new ProductExtraction(
                p.expirationDate(), p.productName(), p.suggestedStorageSpotId(),
                p.productType(), p.priceAmount(), p.currency()
        )).collect(Collectors.toCollection(ArrayList::new));
    }



    private List<ProductExtraction> mergeRectifiedProducts(
            List<ProductCommand> products,
            List<ProductExtraction> rectifiedProducts
    ) {
        Iterator<ProductExtraction> rectifiedProductsIterator = rectifiedProducts.iterator();
        List<ProductExtraction> mergedProducts = new ArrayList<>(products.size());

        for (ProductCommand product : products) {
            if (Boolean.TRUE.equals(product.manuallyEditedExpirationDate())) {
                mergedProducts.add(toProductExtractions(List.of(product)).getFirst());
            } else {
                mergedProducts.add(rectifiedProductsIterator.next());
            }
        }

        return mergedProducts;
    }
}
