package com.isalvama.fresh_keep.modules.admin.infrastructure.web;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetProductMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserRegistrationsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetShoppingReceiptsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetUsersUseCase;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserDetailsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetRegisteredUsersCommand;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.mapper.AdminResponseMapper;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.ProductFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.ProductMetricsFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.ReceiptFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.UserRegistrationFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.RegisteredUsersFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.request.ShoppingReceiptsFilterRequest;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductInfoResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductDetailResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductTypeCountResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ProductMetricResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.DailyReceiptSummaryResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.PageResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ShoppingReceiptDetailResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.ShoppingReceiptSummaryResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.RegisteredUserResponse;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.response.UserDetailsResponse;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints")
public class AdminController {

    private final GetUsersUseCase getUsersUseCase;
    private final GetProductsUseCase getProductsUseCase;
    private final GetShoppingReceiptsUseCase getShoppingReceiptsUseCase;
    private final AdminResponseMapper adminResponseMapper;


    @GetMapping("/metrics/users/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve daily user registration counts")
    public ResponseEntity<List<ProductMetricResponse>> getMetricUserRegistrations(
            @Valid @ModelAttribute UserRegistrationFilterRequest filterRequest) {
        return ResponseEntity.ok(getUsersUseCase
                .getUserRegistrationMetrics(new GetUserRegistrationsCommand(
                        filterRequest.from(), filterRequest.to())).stream()
                .map(adminResponseMapper::toProductMetricResponse)
                .toList());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve registered users")
    public ResponseEntity<PageResponse<RegisteredUserResponse>> getRegisteredUsers(
            @Valid @ModelAttribute RegisteredUsersFilterRequest filterRequest) {
        PageResult<RegisteredUserDto> result = getUsersUseCase.getUsers(new GetRegisteredUsersCommand(
                filterRequest.from(), filterRequest.to(), filterRequest.page(), filterRequest.size()));
        List<RegisteredUserResponse> content = result.content().stream()
                .map(adminResponseMapper::toRegisteredUserResponse)
                .toList();
        return ResponseEntity.ok(new PageResponse<>(content, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve complete user details")
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toUserDetailsResponse(getUsersUseCase.getUser(new GetUserDetailsCommand(id))));
    }


    @GetMapping("/metrics/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve daily product creation counts")
    public ResponseEntity<List<ProductMetricResponse>> getMetricProducts(
            @Valid @ModelAttribute ProductMetricsFilterRequest filterRequest) {
        return ResponseEntity.ok(getProductsUseCase
                .getProductMetrics(new GetProductMetricsCommand(
                        filterRequest.from(),
                        filterRequest.to(),
                        filterRequest.spaceId(),
                        filterRequest.creatorId())).stream()
                .map(adminResponseMapper::toProductMetricResponse)
                .toList());
    }

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
                filterRequest.productType() != null ? filterRequest.productType() : null,
                filterRequest.creatorId() != null ? filterRequest.creatorId() : null,
                filterRequest.shoppingReceiptId() != null ? filterRequest.shoppingReceiptId() : null,
                filterRequest.isDeleted() != null ? filterRequest.isDeleted() : null
        );

        List<ProductInfoResponse> products = getProductsUseCase.getProducts(command).stream()
                .map(adminResponseMapper::toProductInfoResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve product details")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toProductDetailResponse(getProductsUseCase.getProduct(id)));
    }

    @GetMapping("/product-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve product types sorted by number of existing products")
    public ResponseEntity<List<ProductTypeCountResponse>> getProductTypes() {
        List<ProductTypeCountResponse> productTypes = getProductsUseCase.getProductTypes().stream()
                .map(adminResponseMapper::toProductTypeCountResponse)
                .toList();
        return ResponseEntity.ok(productTypes);
    }



    @GetMapping("/metrics/shopping-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve daily shopping receipt counts")
    public ResponseEntity<List<DailyReceiptSummaryResponse>> getMetricShoppingReceipts(
            @Valid @ModelAttribute ReceiptFilterRequest filterRequest) {
        List<DailyReceiptSummaryResponse> summaries = getShoppingReceiptsUseCase
                .getShoppingReceiptMetrics(new GetShoppingReceiptMetricsCommand(
                        filterRequest.from(),
                        filterRequest.to(),
                        filterRequest.spaceId(),
                        filterRequest.creatorId()))
                .stream()
                .map(adminResponseMapper::toDailyReceiptSummaryResponse)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/shopping-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve shopping receipts")
    public ResponseEntity<List<ShoppingReceiptSummaryResponse>> getShoppingReceipts(
            @Valid @ModelAttribute ShoppingReceiptsFilterRequest filterRequest) {
        List<ShoppingReceiptSummaryResponse> content = getShoppingReceiptsUseCase.getShoppingReceipts(new GetShoppingReceiptsCommand(
                        filterRequest.from(), filterRequest.to(), filterRequest.spaceId(), filterRequest.userId())).stream()
                .map(adminResponseMapper::toShoppingReceiptSummaryResponse)
                .toList();
        return ResponseEntity.ok(content);
    }

    @GetMapping("/shopping-receipts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrieve shopping receipt details")
    public ResponseEntity<ShoppingReceiptDetailResponse> getShoppingReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toShoppingReceiptDetailResponse(getShoppingReceiptsUseCase.getShoppingReceipt(id)));
    }
}
