package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web;

import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.dto.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.ShoppingReceipt;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ProcessNewShoppingReceiptMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptResponseMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.request.ProcessNewShoppingReceiptRequest;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.response.SpaceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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
    private final ShoppingReceiptResponseMapper mapper;


    @PostMapping(value = "/receipt-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Process a new Shopping Receipt Image")
    public ResponseEntity<ProcessNewShoppingReceiptResponse> processNewShoppingReceipt(
            @PathVariable(name = "spaceId") @UUID String spaceId,
            @Valid @ModelAttribute ProcessNewShoppingReceiptRequest request,
            @AuthenticationPrincipal(expression = "userId") String userId) {

        ProcessNewShoppingReceiptCommand command = ProcessNewShoppingReceiptMapper.toCommand(spaceId, request, userId);

        ProcessNewShoppingReceiptResult result = processNewShoppingReceiptUseCase.execute(command);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.receiptImageId())
                .toUri();

        return ResponseEntity.created(location).body(mapper.toResponse(result));
    }
}
