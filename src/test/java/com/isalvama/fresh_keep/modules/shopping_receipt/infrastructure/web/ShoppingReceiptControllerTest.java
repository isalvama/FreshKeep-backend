package com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.ProcessNewShoppingReceiptUseCase;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.command.ProcessNewShoppingReceiptCommand;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.ProcessNewShoppingReceiptResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.application.port.in.result.SuggestedStorageSpotResult;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.mapper.ShoppingReceiptResponseMapper;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.ProcessNewShoppingReceiptResponse;
import com.isalvama.fresh_keep.modules.shopping_receipt.infrastructure.web.dto.response.SuggestedStorageSpotResponse;
import org.junit.jupiter.api.Test;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ShoppingReceiptController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@Import(ShoppingReceiptControllerTest.MethodSecurityConfig.class)
class ShoppingReceiptControllerTest {

    @EnableMethodSecurity
    static class MethodSecurityConfig {
        // Minimal stand-in for AppSecurityConfiguration's real SecurityFilterChain (stateless, CSRF disabled -
        // there's no session/cookie auth here to protect against CSRF for), without pulling in JWT beans.
        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .anonymous(AbstractHttpConfigurer::disable)
                    .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    // Without this, Spring Security's fallback is Http403ForbiddenEntryPoint, which
                    // returns 403 for unauthenticated requests too - the real AppSecurityConfiguration
                    // has its own CustomAuthenticationEntryPoint for this; this is a minimal stand-in.
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

    @MockitoBean
    private ProcessNewShoppingReceiptUseCase processNewShoppingReceiptUseCase;

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
        when(mapper.toResponse(any())).thenReturn(
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
}
