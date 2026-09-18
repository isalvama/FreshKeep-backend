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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.MediaType;
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
    @Operation(summary = "Process a new Shopping Receipt Image")
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
    @Operation(summary = "Reprocess, along with the Shopping Receipt Image, the products flagged by the user; and persist the final shopping receipt and products data extracted during receipt processing and reprocessing.")
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
    @Operation(summary = "Confirm and persist the shopping receipt and product data extracted during receipt processing.")
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
