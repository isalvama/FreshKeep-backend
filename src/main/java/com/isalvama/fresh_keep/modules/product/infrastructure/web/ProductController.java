package com.isalvama.fresh_keep.modules.product.infrastructure.web;

import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductsUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductsCommand;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.DeleteProductsRequest;
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
}
