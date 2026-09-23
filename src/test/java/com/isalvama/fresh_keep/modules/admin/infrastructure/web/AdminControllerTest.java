package com.isalvama.fresh_keep.modules.admin.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductTypesUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductQueryDto;
import com.isalvama.fresh_keep.modules.product.application.port.out.dto.ProductTypeCountDto;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
                type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@Import(AdminControllerTest.MethodSecurityConfig.class)
class AdminControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetProductsUseCase getProductsUseCase;

    @MockitoBean
    private GetProductTypesUseCase getProductTypesUseCase;

    @Test
    void getProducts_returnsMappedProductsAndPassesFiltersToUseCase() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID storageSpotId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();
        when(getProductsUseCase.execute(any())).thenReturn(List.of(new ProductQueryDto(
                productId, "Milk", LocalDate.of(2026, 9, 20), storageSpotId,
                "DAIRY", receiptId, BigDecimal.valueOf(1.50), "USD")));

        mockMvc.perform(get("/api/v1/admin/products")
                        .param("sort", "NAME_DESC")
                        .param("size", "10")
                        .param("page", "3")
                        .param("productType", "DAIRY")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(productId.toString()))
                .andExpect(jsonPath("$[0].name").value("Milk"))
                .andExpect(jsonPath("$[0].expirationDate").value("2026-09-20"))
                .andExpect(jsonPath("$[0].actualStorageSpotId").value(storageSpotId.toString()))
                .andExpect(jsonPath("$[0].productType").value("DAIRY"))
                .andExpect(jsonPath("$[0].shoppingReceiptId").value(receiptId.toString()))
                .andExpect(jsonPath("$[0].price").value(1.5))
                .andExpect(jsonPath("$[0].currency").value("USD"));

        ArgumentCaptor<com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand> captor =
                ArgumentCaptor.forClass(com.isalvama.fresh_keep.modules.admin.application.command.GetProductsCommand.class);
        verify(getProductsUseCase).execute(captor.capture());
        assertEquals(ProductSortType.NAME_DESC, captor.getValue().sort());
        assertEquals(10, captor.getValue().size());
        assertEquals(3, captor.getValue().page());
        assertEquals("DAIRY", captor.getValue().productType());
    }

    @Test
    void getProductTypes_returnsMappedCounts() throws Exception {
        when(getProductTypesUseCase.execute()).thenReturn(List.of(
                new ProductTypeCountDto("DAIRY", 8),
                new ProductTypeCountDto("FRUITS", 5)));

        mockMvc.perform(get("/api/v1/admin/product-types").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productType").value("DAIRY"))
                .andExpect(jsonPath("$[0].productCount").value(8))
                .andExpect(jsonPath("$[1].productType").value("FRUITS"))
                .andExpect(jsonPath("$[1].productCount").value(5));

        verify(getProductTypesUseCase).execute();
    }

    @Test
    void getProducts_returnsForbiddenForAuthenticatedNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products").with(asUser()))
                .andExpect(status().isForbidden());

        verify(getProductsUseCase, never()).execute(any());
    }

    private RequestPostProcessor asAdmin() {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                "account-id", "admin@email.com", "hash", null, "admin-id", List.of("ADMIN"));
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    private RequestPostProcessor asUser() {
        CustomUserPrincipal principal = new CustomUserPrincipal(
                "account-id", "user@email.com", "hash", "user-id", null, List.of("USER"));
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }
}
