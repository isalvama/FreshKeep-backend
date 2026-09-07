package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ReProcessShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ReProcessShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptCommandMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptResponseMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProcessNewShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ReProcessShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
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

    @PostMapping(value = "/shopping-receipt")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Reprocess the products flagged by the user with the Shopping Receipt Image and persist the Shopping Receipt and the Products")
    public ResponseEntity<ProcessNewShoppingReceiptResponse> reProcessShoppingReceiptWithFlaggedProducts(
            @PathVariable(name = "spaceId") @UUID String spaceId,
            @Valid @ModelAttribute ReProcessShoppingReceiptRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        ReProcessShoppingReceiptCommand command = commandMapper.toReProcessShoppingReceiptCommand(spaceId, request, userId);

        ReProcessShoppingReceiptResult result = reProcessShoppingReceiptUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return null;
        // TODO return ResponseEntity.created(location).body(responseMapper.toResponse(result));
    }
}
