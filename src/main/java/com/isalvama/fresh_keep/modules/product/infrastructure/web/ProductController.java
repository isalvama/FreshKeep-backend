package com.isalvama.fresh_keep.modules.product.infrastructure.web;

import com.isalvama.fresh_keep.modules.product.application.port.UpdateProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductsUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.MoveProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductsCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.MoveProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.UpdateProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.MoveProductResult;
import com.isalvama.fresh_keep.modules.product.application.port.in.result.UpdateProductResult;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.DeleteProductsRequest;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.MoveProductRequest;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.UpdateProductRequest;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response.MoveProductResponse;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.response.UpdateProductResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.mapper.ProductResponseMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "Products endpoints")
public class ProductController {
    private final DeleteProductUseCase deleteProductUseCase;
    private final DeleteProductsUseCase deleteProductsUseCase;
    private final MoveProductUseCase moveProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductResponseMapper productResponseMapper;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "id") @UUID String productId){

        deleteProductUseCase.execute(new DeleteProductCommand(userId, productId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestBody @Valid DeleteProductsRequest request){

        deleteProductsUseCase.execute(new DeleteProductsCommand(java.util.UUID.fromString(userId), request.productsIds()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/storage-spot")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<MoveProductResponse> move(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "id") @UUID String productId,
            @RequestBody @Valid MoveProductRequest request){

        MoveProductResult result = moveProductUseCase.execute(new MoveProductCommand(java.util.UUID.fromString(userId), java.util.UUID.fromString(productId), request.oldStorageSpotId(), request.newStorageSpotId()));

        MoveProductResponse response = productResponseMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UpdateProductResponse> update(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "id") @UUID String productId,
            @RequestBody @Valid UpdateProductRequest request){

        UpdateProductResult result = updateProductUseCase.execute(
                new UpdateProductCommand(
                java.util.UUID.fromString(userId), java.util.UUID.fromString(productId), request.name(), request.expirationDate(), request.productType(), request.amount(), request.currency()));

        UpdateProductResponse response = productResponseMapper.toResponse(result);

        return ResponseEntity.ok(response);
    }
}
