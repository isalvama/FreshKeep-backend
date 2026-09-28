package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand;
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

import java.util.List;

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
}
