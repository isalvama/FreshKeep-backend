package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ConfirmShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ReProcessShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ConfirmShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ReProcessShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProductResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptCommandMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptResponseMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ShoppingReceiptProductResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.SuggestedStorageSpotResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ShoppingReceiptController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import({ShoppingReceiptControllerTest.MethodSecurityConfig.class, ShoppingReceiptCommandMapper.class})
class ShoppingReceiptControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .anonymous(AbstractHttpConfigurer::disable)
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .exceptionHandling(ex -> ex.authenticationEntryPoint(
                            (request, response, authException) -> response.sendError(401)))
                    .build();
        }
    }

    private static final String BASE_URL = "/api/v1/spaces/%s/receipt-images";
    private static final String SPACE_ID = UUID.randomUUID().toString();
    private static final String USER_ID = UUID.randomUUID().toString();

    @Autowired
    private MockMvc mockMvc;

    private static final String REPROCESS_URL = "/api/v1/spaces/%s/shopping-receipt/reprocess";
    private static final String CONFIRM_URL = "/api/v1/spaces/%s/shopping-receipt/confirm";

    @MockitoBean
    private ProcessNewShoppingReceiptUseCase processNewShoppingReceiptUseCase;

    @MockitoBean
    private ReProcessShoppingReceiptUseCase reProcessShoppingReceiptUseCase;

    @MockitoBean
    private ConfirmShoppingReceiptUseCase confirmShoppingReceiptUseCase;

    @MockitoBean
    private ShoppingReceiptResponseMapper mapper;

    private RequestPostProcessor asUser() {
        CustomUserPrincipal principal = new CustomUserPrincipal("account-id", "user@email.com", "hash", USER_ID, null, List.of("USER"));
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private RequestPostProcessor asAdmin() {
        CustomUserPrincipal principal = new CustomUserPrincipal("account-id", "admin@email.com", "hash", null, "admin-id", List.of("ADMIN"));
        return authentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private MockMultipartFile validFile() {
        return new MockMultipartFile("file", "receipt.jpg", "image/jpeg", "fake-image-content".getBytes());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validReprocessRequest() {
        return post(REPROCESS_URL.formatted(SPACE_ID))
                .param("receiptImageId", UUID.randomUUID().toString())
                .param("shoppingDate", "2026-09-01")
                .param("storeName", "SuperMart")
                .param("flaggedProducts[0].expirationDate", "2026-09-10")
                .param("flaggedProducts[0].productName", "Milk")
                .param("flaggedProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                .param("flaggedProducts[0].productType", "DAIRY")
                .param("flaggedProducts[0].priceAmount", "1.50")
                .param("flaggedProducts[0].currency", "USD")
                .param("allProducts[0].expirationDate", "2026-09-10")
                .param("allProducts[0].productName", "Milk")
                .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                .param("allProducts[0].productType", "DAIRY")
                .param("allProducts[0].priceAmount", "1.50")
                .param("allProducts[0].currency", "USD");
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validConfirmRequest() {
        return post(CONFIRM_URL.formatted(SPACE_ID))
                .param("receiptImageId", UUID.randomUUID().toString())
                .param("shoppingDate", "2026-09-01")
                .param("storeName", "SuperMart")
                .param("allProducts[0].expirationDate", "2026-09-10")
                .param("allProducts[0].productName", "Milk")
                .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                .param("allProducts[0].productType", "DAIRY")
                .param("allProducts[0].priceAmount", "1.50")
                .param("allProducts[0].currency", "USD");
    }

    @Test
    void processNewShoppingReceipt_returns201WithLocationAndMappedBodyOnSuccess() throws Exception {
        ProcessNewShoppingReceiptResult result = new ProcessNewShoppingReceiptResult(
                "receipt-image-id", List.of(new SuggestedStorageSpotResult("fridge-id", "Fridge", "FRIDGE")),
                LocalDate.of(2026, 9, 1), "SuperMart", List.of(), List.of());
        when(processNewShoppingReceiptUseCase.execute(any())).thenReturn(result);

        ProcessNewShoppingReceiptResponse response = new ProcessNewShoppingReceiptResponse(
                "receipt-image-id", List.of(new SuggestedStorageSpotResponse("fridge-id", "Fridge", "FRIDGE")),
                LocalDate.of(2026, 9, 1), "SuperMart", List.of(), List.of());
        when(mapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(multipart(BASE_URL.formatted(SPACE_ID))
                        .file(validFile())
                        .with(asUser()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE_URL.formatted(SPACE_ID) + "/receipt-image-id")))
                .andExpect(jsonPath("$.storeName").value("SuperMart"))
                .andExpect(jsonPath("$.suggestedStorageSpots[0].storageSpotId").value("fridge-id"))
                .andExpect(jsonPath("$.suggestedStorageSpots[0].storageSpotName").value("Fridge"))
                .andExpect(jsonPath("$.suggestedStorageSpots[0].storageSpotType").value("FRIDGE"));
    }

    @Test
    void processNewShoppingReceipt_passesTheSpaceIdFromThePathAndUserIdFromThePrincipalToTheUseCase() throws Exception {
        ProcessNewShoppingReceiptResult result = new ProcessNewShoppingReceiptResult(
                "receipt-image-id", List.of(), LocalDate.now(), "SuperMart", List.of(), List.of());
        when(processNewShoppingReceiptUseCase.execute(any())).thenReturn(result);
        when(mapper.toResponse((ProcessNewShoppingReceiptResult) any())).thenReturn(
                new ProcessNewShoppingReceiptResponse("receipt-image-id", List.of(), LocalDate.now(), "SuperMart", List.of(), List.of()));

        mockMvc.perform(multipart(BASE_URL.formatted(SPACE_ID))
                        .file(validFile())
                        .with(asUser()))
                .andExpect(status().isCreated());

        org.mockito.ArgumentCaptor<ProcessNewShoppingReceiptCommand> captor =
                org.mockito.ArgumentCaptor.forClass(ProcessNewShoppingReceiptCommand.class);
        verify(processNewShoppingReceiptUseCase).execute(captor.capture());

        org.junit.jupiter.api.Assertions.assertEquals(SPACE_ID, captor.getValue().spaceId());
        org.junit.jupiter.api.Assertions.assertEquals(USER_ID, captor.getValue().creatorId());
    }

    @Test
    void processNewShoppingReceipt_returns400WhenFileIsMissing() throws Exception {
        mockMvc.perform(multipart(BASE_URL.formatted(SPACE_ID))
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(processNewShoppingReceiptUseCase);
    }

    @Test
    void processNewShoppingReceipt_returns400WhenSpaceIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(multipart(BASE_URL.formatted("not-a-uuid"))
                        .file(validFile())
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(processNewShoppingReceiptUseCase);
    }

    @Test
    void processNewShoppingReceipt_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(multipart(BASE_URL.formatted(SPACE_ID))
                        .file(validFile()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(processNewShoppingReceiptUseCase);
    }

    @Test
    void processNewShoppingReceipt_returns403WhenAuthenticatedWithoutUserRole() throws Exception {
        mockMvc.perform(multipart(BASE_URL.formatted(SPACE_ID))
                        .file(validFile())
                        .with(asAdmin()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(processNewShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns201WithLocationAndMappedBodyOnSuccess() throws Exception {
        ShoppingReceiptResult result = new ShoppingReceiptResult(
                "shopping-receipt-id", LocalDate.of(2026, 9, 1), "SuperMart",
                List.of(new ProductResult(
                        "product-id", "Milk", LocalDate.of(2026, 9, 10), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")),
                List.of(new SuggestedStorageSpotResult("fridge-id", "Fridge", "FRIDGE")));
        when(reProcessShoppingReceiptUseCase.execute(any())).thenReturn(result);

        ShoppingReceiptResponse response = new ShoppingReceiptResponse(
                "shopping-receipt-id", LocalDate.of(2026, 9, 1), "SuperMart",
                List.of(new ShoppingReceiptProductResponse(
                        "product-id", "Milk", LocalDate.of(2026, 9, 10), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")),
                List.of(new SuggestedStorageSpotResponse("fridge-id", "Fridge", "FRIDGE")));
        when(mapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(validReprocessRequest().with(asUser()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(REPROCESS_URL.formatted(SPACE_ID) + "/shopping-receipt-id")))
                .andExpect(jsonPath("$.storeName").value("SuperMart"))
                .andExpect(jsonPath("$.products[0].productName").value("Milk"))
                .andExpect(jsonPath("$.storageSpots[0].storageSpotId").value("fridge-id"));
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_passesTheSpaceIdFromThePathAndUserIdFromThePrincipalToTheUseCase() throws Exception {
        ShoppingReceiptResult result = new ShoppingReceiptResult(
                "shopping-receipt-id", LocalDate.now(), "SuperMart", List.of(), List.of());
        when(reProcessShoppingReceiptUseCase.execute(any())).thenReturn(result);
        when(mapper.toResponse((ShoppingReceiptResult) any())).thenReturn(
                new ShoppingReceiptResponse("shopping-receipt-id", LocalDate.now(), "SuperMart", List.of(), List.of()));

        mockMvc.perform(validReprocessRequest().with(asUser()))
                .andExpect(status().isCreated());

        ArgumentCaptor<ReProcessShoppingReceiptCommand> captor = ArgumentCaptor.forClass(ReProcessShoppingReceiptCommand.class);
        verify(reProcessShoppingReceiptUseCase).execute(captor.capture());

        assertEquals(SPACE_ID, captor.getValue().spaceId());
        assertEquals(USER_ID, captor.getValue().creatorId());
        assertEquals(1, captor.getValue().flaggedProducts().size());
        assertEquals("Milk", captor.getValue().flaggedProducts().getFirst().productName());
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns400WhenFlaggedProductsIsEmpty() throws Exception {
        mockMvc.perform(post(REPROCESS_URL.formatted(SPACE_ID))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("allProducts[0].expirationDate", "2026-09-10")
                        .param("allProducts[0].productName", "Milk")
                        .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("allProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns400WhenAllProductsIsEmpty() throws Exception {
        mockMvc.perform(post(REPROCESS_URL.formatted(SPACE_ID))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("flaggedProducts[0].expirationDate", "2026-09-10")
                        .param("flaggedProducts[0].productName", "Milk")
                        .param("flaggedProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("flaggedProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns400WhenReceiptImageIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(post(REPROCESS_URL.formatted(SPACE_ID))
                        .param("receiptImageId", "not-a-uuid")
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("flaggedProducts[0].expirationDate", "2026-09-10")
                        .param("flaggedProducts[0].productName", "Milk")
                        .param("flaggedProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("flaggedProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns400WhenSpaceIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(post(REPROCESS_URL.formatted("not-a-uuid"))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("flaggedProducts[0].expirationDate", "2026-09-10")
                        .param("flaggedProducts[0].productName", "Milk")
                        .param("flaggedProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("flaggedProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(validReprocessRequest())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void reProcessShoppingReceiptWithFlaggedProducts_returns403WhenAuthenticatedWithoutUserRole() throws Exception {
        mockMvc.perform(validReprocessRequest().with(asAdmin()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reProcessShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns201WithLocationAndMappedBodyOnSuccess() throws Exception {
        ShoppingReceiptResult result = new ShoppingReceiptResult(
                "shopping-receipt-id", LocalDate.of(2026, 9, 1), "SuperMart",
                List.of(new ProductResult(
                        "product-id", "Milk", LocalDate.of(2026, 9, 10), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")),
                List.of(new SuggestedStorageSpotResult("fridge-id", "Fridge", "FRIDGE")));
        when(confirmShoppingReceiptUseCase.execute(any())).thenReturn(result);

        ShoppingReceiptResponse response = new ShoppingReceiptResponse(
                "shopping-receipt-id", LocalDate.of(2026, 9, 1), "SuperMart",
                List.of(new ShoppingReceiptProductResponse(
                        "product-id", "Milk", LocalDate.of(2026, 9, 10), "fridge-id", "DAIRY", BigDecimal.valueOf(1.5), "USD")),
                List.of(new SuggestedStorageSpotResponse("fridge-id", "Fridge", "FRIDGE")));
        when(mapper.toResponse(result)).thenReturn(response);

        mockMvc.perform(validConfirmRequest().with(asUser()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(CONFIRM_URL.formatted(SPACE_ID) + "/shopping-receipt-id")))
                .andExpect(jsonPath("$.storeName").value("SuperMart"))
                .andExpect(jsonPath("$.products[0].productName").value("Milk"))
                .andExpect(jsonPath("$.storageSpots[0].storageSpotId").value("fridge-id"));
    }

    @Test
    void confirmShoppingReceipt_passesTheSpaceIdFromThePathAndUserIdFromThePrincipalToTheUseCase() throws Exception {
        ShoppingReceiptResult result = new ShoppingReceiptResult(
                "shopping-receipt-id", LocalDate.now(), "SuperMart", List.of(), List.of());
        when(confirmShoppingReceiptUseCase.execute(any())).thenReturn(result);
        when(mapper.toResponse((ShoppingReceiptResult) any())).thenReturn(
                new ShoppingReceiptResponse("shopping-receipt-id", LocalDate.now(), "SuperMart", List.of(), List.of()));

        mockMvc.perform(validConfirmRequest().with(asUser()))
                .andExpect(status().isCreated());

        ArgumentCaptor<ConfirmShoppingReceiptCommand> captor = ArgumentCaptor.forClass(ConfirmShoppingReceiptCommand.class);
        verify(confirmShoppingReceiptUseCase).execute(captor.capture());

        assertEquals(SPACE_ID, captor.getValue().spaceId());
        assertEquals(USER_ID, captor.getValue().creatorId());
        assertEquals(1, captor.getValue().products().size());
        assertEquals("Milk", captor.getValue().products().getFirst().productName());
    }

    @Test
    void confirmShoppingReceipt_returns400WhenAllProductsIsEmpty() throws Exception {
        mockMvc.perform(post(CONFIRM_URL.formatted(SPACE_ID))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns400WhenReceiptImageIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(post(CONFIRM_URL.formatted(SPACE_ID))
                        .param("receiptImageId", "not-a-uuid")
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("allProducts[0].expirationDate", "2026-09-10")
                        .param("allProducts[0].productName", "Milk")
                        .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("allProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns400WhenSpaceIdIsNotAValidUuid() throws Exception {
        mockMvc.perform(post(CONFIRM_URL.formatted("not-a-uuid"))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", "2026-09-01")
                        .param("storeName", "SuperMart")
                        .param("allProducts[0].expirationDate", "2026-09-10")
                        .param("allProducts[0].productName", "Milk")
                        .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("allProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns400WhenShoppingDateIsInTheFuture() throws Exception {
        mockMvc.perform(post(CONFIRM_URL.formatted(SPACE_ID))
                        .param("receiptImageId", UUID.randomUUID().toString())
                        .param("shoppingDate", LocalDate.now().plusDays(1).toString())
                        .param("storeName", "SuperMart")
                        .param("allProducts[0].expirationDate", "2026-09-10")
                        .param("allProducts[0].productName", "Milk")
                        .param("allProducts[0].suggestedStorageSpotId", UUID.randomUUID().toString())
                        .param("allProducts[0].productType", "DAIRY")
                        .with(asUser()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(validConfirmRequest())
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }

    @Test
    void confirmShoppingReceipt_returns403WhenAuthenticatedWithoutUserRole() throws Exception {
        mockMvc.perform(validConfirmRequest().with(asAdmin()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(confirmShoppingReceiptUseCase);
    }
}
