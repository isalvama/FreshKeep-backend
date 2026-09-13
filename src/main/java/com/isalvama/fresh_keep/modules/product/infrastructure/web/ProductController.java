package com.isalvama.fresh_keep.modules.product.infrastructure.web;

import com.isalvama.fresh_keep.modules.product.application.port.in.DeleteProductUseCase;
import com.isalvama.fresh_keep.modules.product.application.port.in.command.DeleteProductCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "Products endpoints")
public class ProductController {
    private final DeleteProductUseCase removeProductUseCase;

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal(expression = "userId") String userId,
            @PathVariable(name = "id") @UUID String productId){

        removeProductUseCase.execute(new DeleteProductCommand(userId, productId));
        return ResponseEntity.noContent().build();
    }
}
