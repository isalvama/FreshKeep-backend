package com.isalvama.fresh_keep.modules.admin.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetProductsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetShoppingReceiptsUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetUsersUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.ProductTypeCountResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.*;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;
import com.isalvama.fresh_keep.modules.admin.infrastructure.web.dto.mapper.AdminResponseMapper;
import com.isalvama.fresh_keep.modules.admin.domain.criteria.ProductSortType;
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
import java.time.Instant;
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
@Import({AdminControllerTest.MethodSecurityConfig.class, AdminResponseMapper.class})
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
    private GetShoppingReceiptsUseCase getAdminReceiptsUseCase;

    @MockitoBean
    private GetUsersUseCase getRegisteredUsersUseCase;

    @Test
    void getProducts_returnsMappedProductsAndPassesFiltersToUseCase() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID storageSpotId = UUID.randomUUID();
        UUID receiptId = UUID.randomUUID();
        when(getProductsUseCase.getProducts(any())).thenReturn(List.of(new ProductResult(
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
        verify(getProductsUseCase).getProducts(captor.capture());
        assertEquals(ProductSortType.NAME_DESC, captor.getValue().sort());
        assertEquals(10, captor.getValue().size());
        assertEquals(3, captor.getValue().page());
        assertEquals("DAIRY", captor.getValue().productType());
    }

    @Test
    void getProductTypes_returnsMappedCounts() throws Exception {
        when(getProductsUseCase.getProductTypes()).thenReturn(List.of(
                new ProductTypeCountResult("DAIRY", 8),
                new ProductTypeCountResult("FRUITS", 5)));

        mockMvc.perform(get("/api/v1/admin/product-types").with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productType").value("DAIRY"))
                .andExpect(jsonPath("$[0].productCount").value(8))
                .andExpect(jsonPath("$[1].productType").value("FRUITS"))
                .andExpect(jsonPath("$[1].productCount").value(5));

        verify(getProductsUseCase).getProductTypes();
    }

    @Test
    void getProducts_returnsForbiddenForAuthenticatedNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/products").with(asUser()))
                .andExpect(status().isForbidden());

        verify(getProductsUseCase, never()).getProducts(any());
    }

    @Test
    void getUserRegistrationMetrics_mapsResultAndPassesDateRange() throws Exception {
        when(getRegisteredUsersUseCase.getUserRegistrationMetrics(any())).thenReturn(
                List.of(new UserRegistrationMetricResult(LocalDate.of(2026, 1, 10), 3)));

        mockMvc.perform(get("/api/v1/admin/metrics/users/registrations")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-10")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-01-10"))
                .andExpect(jsonPath("$[0].count").value(3));

        verify(getRegisteredUsersUseCase).getUserRegistrationMetrics(any());
    }

    @Test
    void getRegisteredUsers_returnsPageResponse() throws Exception {
        RegisteredUserDto user = new RegisteredUserDto(UUID.randomUUID(), "user@email.com", "user",
                Instant.parse("2026-01-01T10:00:00Z"), null);
        when(getRegisteredUsersUseCase.getUsers(any())).thenReturn(PageResult.of(List.of(user), 1, 10, 1));

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("from", "2026-01-01")
                        .param("to", "2026-01-10")
                        .param("page", "1")
                        .param("size", "10")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@email.com"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getUserDetails_mapsUserDetails() throws Exception {
        UUID userId = UUID.randomUUID();
        when(getRegisteredUsersUseCase.getUser(any())).thenReturn(new UserDetailsResult(
                userId, "user@email.com", "user", Instant.parse("2026-01-01T10:00:00Z"), null,
                List.of("USER"), List.of(), List.of()));

        mockMvc.perform(get("/api/v1/admin/users/" + userId).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.email").value("user@email.com"));
    }

    @Test
    void getProductMetrics_mapsResult() throws Exception {
        when(getProductsUseCase.getProductMetrics(any())).thenReturn(
                List.of(new ProductMetricResult(LocalDate.of(2026, 2, 10), 4)));

        mockMvc.perform(get("/api/v1/admin/metrics/products")
                        .param("from", "2026-02-01")
                        .param("to", "2026-02-10")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-02-10"))
                .andExpect(jsonPath("$[0].count").value(4));
    }

    @Test
    void getProductDetails_mapsProductDetail() throws Exception {
        UUID productId = UUID.randomUUID();
        when(getProductsUseCase.getProduct(productId)).thenReturn(new ProductDetailResult(
                productId, "Milk", "DAIRY", LocalDate.of(2026, 3, 1), UUID.randomUUID(), "FRIDGE",
                null, null, null, null, null, null, null, Instant.parse("2026-01-01T10:00:00Z"),
                null, BigDecimal.valueOf(1.5), "USD"));

        mockMvc.perform(get("/api/v1/admin/products/" + productId).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Milk"))
                .andExpect(jsonPath("$.price").value(1.5));
    }

    @Test
    void getShoppingReceiptMetrics_mapsResult() throws Exception {
        when(getAdminReceiptsUseCase.getShoppingReceiptMetrics(any())).thenReturn(
                List.of(new DailyReceiptSummaryResult(LocalDate.of(2026, 4, 10), 2)));

        mockMvc.perform(get("/api/v1/admin/metrics/shopping-receipts")
                        .param("from", "2026-04-01")
                        .param("to", "2026-04-10")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-04-10"))
                .andExpect(jsonPath("$[0].totalReceipts").value(2));
    }

    @Test
    void getShoppingReceipts_mapsReceiptSummaries() throws Exception {
        UUID receiptId = UUID.randomUUID();
        when(getAdminReceiptsUseCase.getShoppingReceipts(any())).thenReturn(List.of(new ReceiptSummaryResult(
                receiptId, UUID.randomUUID(), UUID.randomUUID(), "Store", LocalDate.of(2026, 5, 1),
                Instant.parse("2026-05-01T10:00:00Z"))));

        mockMvc.perform(get("/api/v1/admin/shopping-receipts")
                        .param("from", "2026-05-01")
                        .param("to", "2026-05-01")
                        .with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(receiptId.toString()))
                .andExpect(jsonPath("$[0].storeName").value("Store"));
    }

    @Test
    void getShoppingReceiptDetails_mapsReceiptDetail() throws Exception {
        UUID receiptId = UUID.randomUUID();
        when(getAdminReceiptsUseCase.getShoppingReceipt(receiptId)).thenReturn(new ReceiptDetailResult(
                receiptId, null, "owner", "owner@email.com", null, null, "Store",
                LocalDate.of(2026, 6, 1), Instant.parse("2026-06-01T10:00:00Z"), null, null, null, List.of()));

        mockMvc.perform(get("/api/v1/admin/shopping-receipts/" + receiptId).with(asAdmin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receiptId.toString()))
                .andExpect(jsonPath("$.storeName").value("Store"));
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
