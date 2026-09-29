package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetShoppingReceiptsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.out.ShoppingReceiptQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyReceiptSummaryDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptDetailDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptProductDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.ReceiptSummaryDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetShoppingReceiptsServiceTest {
    @Mock
    private ShoppingReceiptQueryPort queryPort;
    @InjectMocks
    private GetShoppingReceiptsService service;

    @Test
    void getShoppingReceiptMetrics_mapsResultsAndPassesFilters() {
        UUID spaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        GetShoppingReceiptMetricsCommand command = new GetShoppingReceiptMetricsCommand(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), spaceId, creatorId);
        when(queryPort.getShoppingReceiptMetrics(command.from(), command.to(), spaceId, creatorId))
                .thenReturn(List.of(new DailyReceiptSummaryDto(command.from(), 2)));

        assertEquals(2, service.getShoppingReceiptMetrics(command).getFirst().totalReceipts());
    }

    @Test
    void getShoppingReceipts_mapsSummaries() {
        ReceiptSummaryDto receipt = new ReceiptSummaryDto(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Store", LocalDate.of(2026, 1, 1), Instant.parse("2026-01-01T10:00:00Z"));
        GetShoppingReceiptsCommand command = new GetShoppingReceiptsCommand(null, null, null, null);
        when(queryPort.findReceipts(null, null, null, null)).thenReturn(List.of(receipt));

        assertEquals(receipt.id(), service.getShoppingReceipts(command).getFirst().id());
    }

    @Test
    void getShoppingReceipt_mapsDetailsAndProducts() {
        UUID receiptId = UUID.randomUUID();
        ReceiptProductDto product = new ReceiptProductDto(UUID.randomUUID(), "Milk", LocalDate.of(2026, 2, 1),
                UUID.randomUUID(), "DAIRY", BigDecimal.ONE, "USD");
        ReceiptDetailDto receipt = new ReceiptDetailDto(receiptId, UUID.randomUUID(), "owner", "owner@email.com",
                UUID.randomUUID(), "Kitchen", "Store", LocalDate.of(2026, 1, 1),
                Instant.parse("2026-01-01T10:00:00Z"), null, null, null, List.of(product));
        when(queryPort.findReceiptById(receiptId)).thenReturn(Optional.of(receipt));

        assertEquals(1, service.getShoppingReceipt(receiptId).products().size());
    }

    @Test
    void getShoppingReceipt_throwsWhenReceiptDoesNotExist() {
        UUID receiptId = UUID.randomUUID();
        when(queryPort.findReceiptById(receiptId)).thenReturn(Optional.empty());

        assertThrows(NonExistentShoppingReceiptException.class, () -> service.getShoppingReceipt(receiptId));
    }
}
