package com.isalvama.fresh_keep.modules.admin.infrastructure.web;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductTypesUseCase;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.mapper.AdminResponseMapper;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.ProductFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductInfoResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductTypeCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints")
public class AdminController {

    private final GetProductsUseCase getProductsUseCase;
    private final GetProductTypesUseCase getProductTypesUseCase;
    private final AdminResponseMapper adminResponseMapper;

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve generated products data")
    public ResponseEntity<List<ProductInfoResponse>> getProducts(
            @Valid @ModelAttribute ProductFilterRequest filterRequest
            ) {

        GetProductsCommand command = new GetProductsCommand(
                filterRequest.sort(),
                filterRequest.size(),
                filterRequest.page(),
                filterRequest.productType() != null ? filterRequest.productType() : null
        );

        List<ProductInfoResponse> products = getProductsUseCase.execute(command).stream()
                .map(adminResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/product-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve product types sorted by number of existing products")
    public ResponseEntity<List<ProductTypeCountResponse>> getProductTypes() {
        List<ProductTypeCountResponse> productTypes = getProductTypesUseCase.execute().stream()
                .map(adminResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(productTypes);
    }
}
