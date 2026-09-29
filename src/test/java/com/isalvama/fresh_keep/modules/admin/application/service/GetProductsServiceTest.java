package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetProductMetricsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductDetailDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductDetailResult;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.GetAllProductsDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductQueryPort;
import com.isalvama.fresh_keep.modules.product.domain.exception.InvalidProductTypeException;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductsServiceTest {

    @Mock
    private ProductQueryPort productQueryPort;

    @InjectMocks
    private GetProductsService service;

    @Test
    void execute_convertsPageSizeAndProductTypeBeforeQuerying() {
        GetProductsCommand command = new GetProductsCommand(
                ProductSortType.NAME_ASC, 10, 3, "DAIRY", null, null, null);
        when(productQueryPort.getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 20, 10, ProductType.DAIRY, null, null)))
                .thenReturn(List.of());

        List<?> result = service.getProducts(command);

        assertEquals(List.of(), result);
        verify(productQueryPort).getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 20, 10, ProductType.DAIRY, null, null));
    }

    @Test
    void execute_passesNullProductTypeWhenFilterIsAbsent() {
        GetProductsCommand command = new GetProductsCommand(
                ProductSortType.EXPIRATION_DATE_DESC, null, null, null, null, null, null);
        when(productQueryPort.getAllProducts(new GetAllProductsDto(ProductSortType.EXPIRATION_DATE_DESC, 0, 30, null, null, null)))
                .thenReturn(List.of());

        service.getProducts(command);

        verify(productQueryPort).getAllProducts(new GetAllProductsDto(ProductSortType.EXPIRATION_DATE_DESC, 0, 30, null, null, null));
    }

    @Test
    void execute_throwsWhenProductTypeIsInvalid() {
        GetProductsCommand command = new GetProductsCommand(
                ProductSortType.NAME_ASC, 10, 1, "INVALID", null, null, null);

        assertThrows(InvalidProductTypeException.class, () -> service.getProducts(command));
    }

    @Test
    void getProductMetrics_mapsQueryResultsAndPassesFilters() {
        UUID spaceId = UUID.randomUUID();
        UUID creatorId = UUID.randomUUID();
        GetProductMetricsCommand command = new GetProductMetricsCommand(
                java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 1, 10), spaceId, creatorId);
        when(productQueryPort.getProducts(command.from(), command.to(), spaceId, creatorId))
                .thenReturn(List.of(new DailyMetricDto(command.from(), 3)));

        assertEquals(3, service.getProductMetrics(command).getFirst().count());
        verify(productQueryPort).getProducts(command.from(), command.to(), spaceId, creatorId);
    }

    @Test
    void getProducts_mapsProductsReturnedByQueryPort() {
        UUID productId = UUID.randomUUID();
        ProductQueryDto product = new ProductQueryDto(productId, "Milk", java.time.LocalDate.of(2026, 2, 1),
                UUID.randomUUID(), "DAIRY", UUID.randomUUID(), BigDecimal.valueOf(1.5), "USD");
        GetProductsCommand command = new GetProductsCommand(ProductSortType.NAME_ASC, 10, 1, null, null, null, null);
        when(productQueryPort.getAllProducts(new GetAllProductsDto(ProductSortType.NAME_ASC, 0, 10, null, null, null)))
                .thenReturn(List.of(product));

        assertEquals(productId, service.getProducts(command).getFirst().id());
    }

    @Test
    void getProduct_mapsProductDetails() {
        UUID productId = UUID.randomUUID();
        ProductDetailDto product = new ProductDetailDto(productId, "Milk", "DAIRY",
                java.time.LocalDate.of(2026, 2, 1), UUID.randomUUID(), "FRIDGE", UUID.randomUUID(),
                "owner", "owner@email.com", UUID.randomUUID(), "Kitchen", "Store",
                java.time.LocalDate.of(2026, 1, 31), java.time.Instant.parse("2026-01-31T10:00:00Z"),
                UUID.randomUUID(), BigDecimal.valueOf(1.5), "USD");
        when(productQueryPort.getProductById(productId)).thenReturn(Optional.of(product));

        ProductDetailResult result = service.getProduct(productId);

        assertEquals(productId, result.id());
        assertEquals("owner", result.creatorUsername());
    }

    @Test
    void getProduct_throwsWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        when(productQueryPort.getProductById(productId)).thenReturn(Optional.empty());

        assertThrows(com.isalvama.fresh_keep.modules.product.domain.exception.NonExistentProductException.class,
                () -> service.getProduct(productId));
    }

    @Test
    void getProductTypes_mapsCountsReturnedByQueryPort() {
        when(productQueryPort.getProductTypesByCount())
                .thenReturn(List.of(new ProductTypeCountDto("DAIRY", 4)));

        assertEquals(4, service.getProductTypes().getFirst().productCount());
    }
}
