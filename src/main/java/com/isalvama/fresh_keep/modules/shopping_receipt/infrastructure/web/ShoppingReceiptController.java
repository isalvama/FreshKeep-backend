package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ConfirmShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ReProcessShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptCommandMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptResponseMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ConfirmShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProcessNewShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ReProcessShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ShoppingReceiptResponse;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/spaces/{spaceId}")
@RequiredArgsConstructor
@Validated
@Tag(name = "ShoppingReceipts", description = "ShoppingReceipts endpoints")
public class ShoppingReceiptController {
    private final ProcessNewShoppingReceiptUseCase processNewShoppingReceiptUseCase;
    private final ReProcessShoppingReceiptUseCase reProcessShoppingReceiptUseCase;
    private final ConfirmShoppingReceiptUseCase confirmShoppingReceiptUseCase;
    private final ShoppingReceiptResponseMapper responseMapper;
    private final ShoppingReceiptCommandMapper commandMapper;

    @PostMapping(value = "/receipt-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Process a new Shopping Receipt Image",
            description = "Runs AI extraction on the uploaded image and persists a draft ShoppingReceipt and its " +
                    "ReceiptImage. No Product is persisted yet — review the response, then call reprocess or " +
                    "confirm with the returned shoppingReceiptId to finalize it. purchaseShoppingDate is already " +
                    "rectified server-side (clamped to today if the AI extracted a future date, with every " +
                    "product's expirationDate shifted by the same number of days)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Draft receipt created",
                    content = @Content(schema = @Schema(implementation = ProcessNewShoppingReceiptResponse.class))),
            @ApiResponse(responseCode = "400", description = "file missing or the multipart body is malformed, file is present but " +
                    "empty, spaceId path variable is not a valid UUID, the space does not exist, or language is blank",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of spaceId",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The AI could not process the image at all, e.g. not a readable receipt",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "The AI provider's rate limit was hit",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected AI-side failure, or an unexpected persistence failure while " +
                    "looking up the space/participant, or saving the receipt image or the draft shopping receipt",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "The AI call completed but returned an empty/unparseable response",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "The AI call could not be completed (unreachable/unavailable) in a " +
                    "retryable way after retries exhausted; response includes a Retry-After header (seconds)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProcessNewShoppingReceiptResponse> processNewShoppingReceipt(
            @PathVariable(name = "spaceId") @UUID String spaceId,
            @Valid @ModelAttribute ProcessNewShoppingReceiptRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        ProcessNewShoppingReceiptCommand command = commandMapper.toProcessNewShoppingReceiptCommand(spaceId, request, userId);

        ProcessNewShoppingReceiptResult result = processNewShoppingReceiptUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.receiptImageId())
                .toUri();

        return ResponseEntity.created(location).body(responseMapper.toResponse(result));
    }

    @PostMapping(value = "/shopping-receipt/reprocess")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Reprocess with flagged products",
            description = "Re-runs AI extraction (seeded with allProducts and the specific flaggedProducts) against " +
                    "the already-uploaded receipt image, then finalizes the draft ShoppingReceipt referenced by " +
                    "shoppingReceiptId and persists its Products. Unlike processNewShoppingReceipt, there is no " +
                    "fallback if the AI call fails outright."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shopping receipt finalized and persisted",
                    content = @Content(schema = @Schema(implementation = ShoppingReceiptResponse.class))),
            @ApiResponse(responseCode = "400", description = "A field fails bean validation (missing/blank/empty/future shoppingDate/" +
                    "missing manuallyEditedExpirationDate on any product), the request body is malformed/missing, spaceId/" +
                    "receiptImageId/shoppingReceiptId is not a valid UUID, the space does not exist, receiptImageId/" +
                    "shoppingReceiptId does not reference an existing record, the ids/creator/space don't all belong to the same " +
                    "draft, a product's productType/currency is not a recognized constant name, or a product's productName has no " +
                    "letter",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of spaceId",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The AI could not process the image at all, e.g. not a readable receipt",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "429", description = "The AI provider's rate limit was hit",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected AI-side failure, or an unexpected persistence failure while " +
                    "looking up the space/participant, updating the shopping receipt, or registering its products, or an unexpected failure retrieving the receipt image",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "502", description = "The AI call completed but returned an empty/unparseable response",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "503", description = "The AI call could not be completed (unreachable/unavailable) in a " +
                    "retryable way after retries exhausted; response includes a Retry-After header (seconds)",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ShoppingReceiptResponse> reprocessWithFlaggedProducts(
            @PathVariable(name = "spaceId") @UUID String spaceId,
            @Valid @RequestBody ReProcessShoppingReceiptRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        ReProcessShoppingReceiptCommand command = commandMapper.toReProcessShoppingReceiptCommand(spaceId, request, userId);

        ShoppingReceiptResult result = reProcessShoppingReceiptUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.shoppingReceiptId())
                .toUri();

        return ResponseEntity.created(location).body(responseMapper.toResponse(result));
    }

    @PostMapping(value = "/shopping-receipt/confirm")
    @PreAuthorize("hasRole('USER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Confirm and persist the shopping receipt",
            description = "Skips AI re-extraction and persists the client-submitted data as-is, finalizing the " +
                    "draft ShoppingReceipt referenced by shoppingReceiptId. If the submitted shoppingDate differs " +
                    "from the draft's purchase date, the expirationDate of every product with " +
                    "manuallyEditedExpirationDate: false is shifted by the same number of days; products with " +
                    "manuallyEditedExpirationDate: true are saved exactly as submitted. receiptImageId is only " +
                    "checked against the draft's own stored value here, not looked up independently."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Shopping receipt finalized and persisted",
                    content = @Content(schema = @Schema(implementation = ShoppingReceiptResponse.class))),
            @ApiResponse(responseCode = "400", description = "A field fails bean validation (missing/blank/empty/future shoppingDate/" +
                    "missing manuallyEditedExpirationDate on any product), the request body is malformed/missing, spaceId/" +
                    "receiptImageId/shoppingReceiptId is not a valid UUID, the space does not exist, shoppingReceiptId does not " +
                    "reference an existing record, the ids/creator/space don't all belong to the same draft (this is also how an " +
                    "receiptImageId that doesn't match the draft surfaces), a product's productType/currency is not a recognized " +
                    "constant name, or a product's productName has no letter",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the USER role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "The caller is not a participant of spaceId",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure while looking up the space/participant, " +
                    "updating the shopping receipt, or registering its products",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ShoppingReceiptResponse> confirmShoppingReceipt(
            @PathVariable(name = "spaceId") @UUID String spaceId,
            @Valid @RequestBody ConfirmShoppingReceiptRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        ConfirmShoppingReceiptCommand command = commandMapper.toConfirmShoppingReceiptCommand(spaceId, request, userId);

        ShoppingReceiptResult result = confirmShoppingReceiptUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.shoppingReceiptId())
                .toUri();

        return ResponseEntity.created(location).body(responseMapper.toResponse(result));
    }
}
