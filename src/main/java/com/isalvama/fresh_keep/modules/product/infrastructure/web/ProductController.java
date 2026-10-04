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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete a product",
            description = "Soft delete — sets the product's deletedAt timestamp rather than removing the row."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "400", description = "id path variable is not a valid UUID, or the product does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of the product's storage spot's space, or " +
                    "the product was concurrently modified/deleted by someone else (retry)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while deleting the product: failure in the product's data retrieval, or the information to validate the existence of the storage spot and the participancy of the user in it or in the deleting process",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "id") @UUID String productId){

        deleteProductUseCase.execute(new DeleteProductCommand(userId, productId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping()
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete multiple products",
            description = "Batch version of the single delete, same soft-delete semantics. All-or-nothing: a missing " +
                    "id or a product in a space the caller can't access fails the whole request, nothing is deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Products deleted"),
            @ApiResponse(responseCode = "400", description = "productsIds missing/empty, any element is not a valid UUID, " +
                    "or one or more of the referenced products do not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of one or more of the products' spaces, or " +
                    "one or more products were concurrently modified/deleted by someone else (retry)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while deleting the products: failure in the product's data retrieval, or the information to validate the existence of the storage spot and the participancy of the user in it or in the deleting process",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @RequestBody @Valid DeleteProductsRequest request){

        deleteProductsUseCase.execute(new DeleteProductsCommand(java.util.UUID.fromString(userId), request.productsIds()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/storage-spot")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Move a product to a new storage spot",
            description = "Both spots' spaces must be accessible to the caller. The product's expirationDate is " +
                    "recalculated by an AI (Ollama) call seeded with the product's storage-spot history; the " +
                    "returned newExpirationDate may differ from the product's previous date. This call needs the " +
                    "backend's Ollama service reachable and has no silent fallback on AI failure."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product moved",
                    content = @Content(schema = @Schema(implementation = MoveProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "oldStorageSpotId/newStorageSpotId missing, id path variable not a valid UUID, " +
                    "the product does not exist, or oldStorageSpotId does not match the product's current spot",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of the old and/or new spot's space(s)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Bad/missing data for the product's expiration date recalculation, supporting data (shopping date / " +
                    "storage-spot history / spot info) could not be resolved, or an unexpected persistence failure while saving the move or " +
                    "in the product, storage spot history, shopping date or storage spot data retrieval",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "AI expiration-date recalculation failed in a retryable way after retries " +
                    "exhausted; response includes a Retry-After header (seconds)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Partially update a product",
            description = "Every field is optional; omitted/null fields keep their current value. An empty body {} " +
                    "is a valid no-op that returns the product's current state."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = UpdateProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "A field fails bean validation (too long / spaces-only / non-positive " +
                    "amount), id path variable is not a valid UUID, the product does not exist, name has no letter, or productType/" +
                    "currency is not a recognized constant name",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of the product's storage spot's space",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while saving the new data of the product or " +
                    "in the product, or storage spot data and the user's participancy retrieval",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
