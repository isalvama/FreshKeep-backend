package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ConfirmShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProductCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ProductRegistrationPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ReceiptImageRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.ShoppingReceiptRepositoryPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.SpaceLookUpPort;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentReceiptImageException;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ReceiptImage;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ReceiptImageId;
import com.isalvama.fresh_keep.modules.space.domain.model.value_object.SpaceId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConfirmShoppingReceiptService implements ConfirmShoppingReceiptUseCase {
    private final SpaceLookUpPort spaceLookUpPort;
    private final ReceiptImageRepositoryPort receiptImageRepositoryPort;
    private final ShoppingReceiptRepositoryPort shoppingReceiptRepositoryPort;
    private final StorageSpotSuggestionResolver storageSpotSuggestionResolver;
    private final ProductRegistrationPort productRegistrationPort;
    private final Clock clock;


    @Override
    public ShoppingReceiptResult execute(ConfirmShoppingReceiptCommand command) {

        List<StorageSpotDto> storageSpotDtos = spaceLookUpPort.getStorageSpotsBySpaceIdAndParticipantId(
                GetStorageSpotsDto.create(command.spaceId(), command.creatorId())
        );

        ReceiptImage receiptImage = receiptImageRepositoryPort.findById(ReceiptImageId.from(command.receiptImageId()))
                .orElseThrow(() -> new NonExistentReceiptImageException("Receipt Image with id " + command.receiptImageId() + " does not exist."));

        List<ProductExtraction> resolvedProductExtractions = storageSpotSuggestionResolver.resolve(toProductExtractions(command.products()), storageSpotDtos);

        ShoppingReceipt shoppingReceipt = ShoppingReceipt.create(
                UserId.from(command.creatorId()),
                SpaceId.from(command.spaceId()),
                receiptImage.getId(),
                command.shoppingDate(),
                command.storeName(),
                clock
        );

        shoppingReceiptRepositoryPort.save(shoppingReceipt);

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

        return new ShoppingReceiptResult(
                shoppingReceipt.getId().toString(),
                shoppingReceipt.getPurchaseDate(),
                shoppingReceipt.getStoreName(),
                registeredProducts.stream().map(ProductResult::toResult).toList(),
                storageSpotDtos.stream().map(SuggestedStorageSpotResult::fromStorageSpotDto).toList()
        );
    }


    private List<ProductExtraction> toProductExtractions(List<ProductCommand> products) {
        return products.stream().map(p -> new ProductExtraction(
                p.expirationDate(), p.productName(), p.suggestedStorageSpotId(),
                p.productType(), p.priceAmount(), p.currency()
        )).toList();
    }
}
