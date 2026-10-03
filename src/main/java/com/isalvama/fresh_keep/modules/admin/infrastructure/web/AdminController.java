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
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptSummaryResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve daily user registration counts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics retrieved",
                    content = @Content(schema = @Schema(implementation = ProductMetricResponse.class))),
            @ApiResponse(responseCode = "400", description = "from/to missing or malformed, or the date range spans more than 100 days",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve registered users", description = "page is one-based; defaults to 1. size defaults to 30, maximum 40.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users retrieved",
                    content = @Content(schema = @Schema(implementation = RegisteredUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "from/to missing or malformed, the date range spans more than 90 days, " +
                    "or page/size are out of range",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve complete user details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User details retrieved",
                    content = @Content(schema = @Schema(implementation = UserDetailsResponse.class))),
            @ApiResponse(responseCode = "400", description = "id path variable is not a valid UUID, or the user does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<UserDetailsResponse> getUserDetails(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toUserDetailsResponse(getUsersUseCase.getUser(new GetUserDetailsCommand(id))));
    }


    @GetMapping("/metrics/products")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve daily product creation counts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics retrieved",
                    content = @Content(schema = @Schema(implementation = ProductMetricResponse.class))),
            @ApiResponse(responseCode = "400", description = "from/to missing or malformed, or the date range spans more than 100 days",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the metrics",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve generated products data",
            description = "page is 1-based (missing or 0 means the first page); size defaults to 30, maximum 40. " +
                    "Filters combine with AND. Deleted products are never returned. isDeleted is accepted by the " +
                    "request model but currently ignored. This is a plain list with no total count: a page with " +
                    "fewer than size items is the last one, and a page past the end returns []."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved",
                    content = @Content(schema = @Schema(implementation = ProductInfoResponse.class))),
            @ApiResponse(responseCode = "400", description = "productType is not a recognized ProductType, creatorId/shoppingReceiptId " +
                    "is not a valid UUID, or page/size are out of range",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the products",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve product details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product details retrieved",
                    content = @Content(schema = @Schema(implementation = ProductDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "id path variable is not a valid UUID, or the product does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the product",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toProductDetailResponse(getProductsUseCase.getProduct(id)));
    }

    @GetMapping("/product-types")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve product types sorted by number of existing products")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product type counts retrieved",
                    content = @Content(schema = @Schema(implementation = ProductTypeCountResponse.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while retrieving the product type counts",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<List<ProductTypeCountResponse>> getProductTypes() {
        List<ProductTypeCountResponse> productTypes = getProductsUseCase.getProductTypes().stream()
                .map(adminResponseMapper::toProductTypeCountResponse)
                .toList();
        return ResponseEntity.ok(productTypes);
    }



    @GetMapping("/metrics/shopping-receipts")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve daily shopping receipt counts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Metrics retrieved",
                    content = @Content(schema = @Schema(implementation = DailyReceiptSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "from/to missing or malformed, or the date range spans more than 100 days",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
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
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve shopping receipts", description = "page is one-based; defaults to 1. size defaults to 30, maximum 40.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping receipts retrieved",
                    content = @Content(schema = @Schema(implementation = ShoppingReceiptSummaryResponse.class))),
            @ApiResponse(responseCode = "400", description = "from/to missing or malformed, the date range spans more than 100 days, " +
                    "spaceId/userId is not a valid UUID, or page/size are out of range",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<PageResponse<ShoppingReceiptSummaryResponse>> getShoppingReceipts(
            @Valid @ModelAttribute ShoppingReceiptsFilterRequest filterRequest) {
        PageResult<ReceiptSummaryResult> result = getShoppingReceiptsUseCase.getShoppingReceipts(
                new GetShoppingReceiptsCommand(filterRequest.from(), filterRequest.to(), filterRequest.spaceId(),
                        filterRequest.userId(), filterRequest.page(), filterRequest.size()));
        List<ShoppingReceiptSummaryResponse> content = result.content().stream()
                .map(adminResponseMapper::toShoppingReceiptSummaryResponse)
                .toList();
        return ResponseEntity.ok(new PageResponse<>(content, result.page(), result.size(),
                result.totalElements(), result.totalPages()));
    }

    @GetMapping("/shopping-receipts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Retrieve shopping receipt details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Shopping receipt details retrieved",
                    content = @Content(schema = @Schema(implementation = ShoppingReceiptDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "id path variable is not a valid UUID, or the shopping receipt does not exist",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ShoppingReceiptDetailResponse> getShoppingReceipt(@PathVariable UUID id) {
        return ResponseEntity.ok(adminResponseMapper.toShoppingReceiptDetailResponse(getShoppingReceiptsUseCase.getShoppingReceipt(id)));
    }
}
