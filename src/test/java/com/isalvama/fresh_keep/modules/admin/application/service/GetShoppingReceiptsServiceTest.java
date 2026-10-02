package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptDetailResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ReceiptSummaryResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptsPageDto;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.NonExistentShoppingReceiptException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShoppingReceiptsServiceTest {

    private static final LocalDate FROM = LocalDate.of(2026, 9, 1);
    private static final LocalDate TO = LocalDate.of(2026, 9, 30);

    @Mock
    private ShoppingReceiptQueryPort receiptQueryPort;

    @InjectMocks
    private GetShoppingReceiptsService service;

    @Test
    void getShoppingReceipts_turnsPageAndSizeIntoOffsetAndLimit() {
        UUID spaceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(receiptQueryPort.findReceipts(FROM, TO, spaceId, userId, 20, 10))
                .thenReturn(new ReceiptsPageDto(List.of(), 0));

        service.getShoppingReceipts(new GetShoppingReceiptsCommand(FROM, TO, spaceId, userId, 3, 10));

        verify(receiptQueryPort).findReceipts(FROM, TO, spaceId, userId, 20, 10);
    }

    @Test
    void getShoppingReceipts_defaultsToTheFirstPageOf30() {
        when(receiptQueryPort.findReceipts(FROM, TO, null, null, 0, 30))
                .thenReturn(new ReceiptsPageDto(List.of(), 0));

        PageResult<ReceiptSummaryResult> page = service.getShoppingReceipts(
                new GetShoppingReceiptsCommand(FROM, TO, null, null, null, null));

        assertEquals(1, page.page());
        assertEquals(30, page.size());
        assertEquals(0, page.totalPages());
    }

    @Test
    void getShoppingReceipts_mapsEveryFieldAndTheTotal() {
        ReceiptSummaryDto receipt = new ReceiptSummaryDto(
                UUID.randomUUID(), UUID.randomUUID(), "alice@email.com", "alice",
                UUID.randomUUID(), "Kitchen", "SuperMart", LocalDate.of(2026, 9, 8),
                Instant.parse("2026-09-08T10:00:00Z"), 4);
        when(receiptQueryPort.findReceipts(FROM, TO, null, null, 30, 30))
                .thenReturn(new ReceiptsPageDto(List.of(receipt), 61));

        PageResult<ReceiptSummaryResult> page = service.getShoppingReceipts(
                new GetShoppingReceiptsCommand(FROM, TO, null, null, 2, 30));

        assertEquals(List.of(new ReceiptSummaryResult(
                receipt.id(), receipt.creatorId(), "alice@email.com", "alice",
                receipt.spaceId(), "Kitchen", "SuperMart", LocalDate.of(2026, 9, 8),
                Instant.parse("2026-09-08T10:00:00Z"), 4)), page.content());
        assertEquals(2, page.page());
        assertEquals(61, page.totalElements());
        assertEquals(3, page.totalPages());
    }

    @Test
    void getShoppingReceipt_carriesTheDeletedFlagOfEachProduct() {
        UUID receiptId = UUID.randomUUID();
        ReceiptProductDto milk = new ReceiptProductDto(UUID.randomUUID(), "Milk", LocalDate.of(2026, 9, 20),
                UUID.randomUUID(), "DAIRY", new BigDecimal("1.50"), "USD", false);
        ReceiptProductDto bread = new ReceiptProductDto(UUID.randomUUID(), "Bread", LocalDate.of(2026, 9, 12),
                UUID.randomUUID(), "BAKERY", null, null, true);
        when(receiptQueryPort.findReceiptById(receiptId)).thenReturn(Optional.of(new ReceiptDetailDto(
                receiptId, null, null, null, null, null, "SuperMart", LocalDate.of(2026, 9, 8),
                Instant.parse("2026-09-08T10:00:00Z"), null, null, null, List.of(milk, bread))));

        ReceiptDetailResult receipt = service.getShoppingReceipt(receiptId);

        assertEquals(List.of(false, true),
                receipt.products().stream().map(ReceiptDetailResult.ProductResult::deleted).toList());
    }

    @Test
    void getShoppingReceipt_throwsForAnUnknownReceipt() {
        UUID receiptId = UUID.randomUUID();
        when(receiptQueryPort.findReceiptById(receiptId)).thenReturn(Optional.empty());

        assertThrows(NonExistentShoppingReceiptException.class, () -> service.getShoppingReceipt(receiptId));
    }
}
