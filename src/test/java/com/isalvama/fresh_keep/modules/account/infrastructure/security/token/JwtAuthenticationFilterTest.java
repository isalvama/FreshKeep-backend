package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import org.junit.jupiter.api.Test;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

    @ExtendWith(MockitoExtension.class)
    class JwtAuthenticationFilterTest {

        @Mock
        private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

        @InjectMocks
        private JwtAuthenticationFilter jwtAuthenticationFilter;

        private MockHttpServletRequest request;
        private MockHttpServletResponse response;
        private FilterChain filterChain;

        @BeforeEach
        void setUp() {
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
            filterChain = mock(FilterChain.class);

            SecurityContextHolder.clearContext();
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

        @Test
        void doFilterInternal_shouldContinueFilterChainAndNotAuthenticateIfHeaderIsMissing() throws ServletException, IOException {
            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtTokenGeneratorAdapter);
        }

        @Test
        void doFilterInternal_shouldContinueFilterChainAndNotAuthenticateIfAuthHeaderPrefixIsInvalid() throws ServletException, IOException {
            request.addHeader("Authorization", "Basic some-base64-string");

            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
            verifyNoInteractions(jwtTokenGeneratorAdapter);
        }

        @Test
        void doFilterInternal_shouldAuthenticateIfTokenIsValid() throws ServletException, IOException {
            String token = "valid.jwt.token";
            request.addHeader("Authorization", "Bearer " + token);

            CustomUserPrincipal mockPrincipal = new CustomUserPrincipal("accountid", "test@test.com", null, "userid", null, List.of("USER"));

            when(jwtTokenGeneratorAdapter.isTokenValid(token)).thenReturn(true);
            when(jwtTokenGeneratorAdapter.extractCustomUserPrincipal(token)).thenReturn(mockPrincipal);

            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            assertThat(authentication).isNotNull();
            assertThat(authentication.getPrincipal()).isEqualTo(mockPrincipal);
            assertThat(authentication.getAuthorities()).hasSize(1);

            verify(jwtTokenGeneratorAdapter).isTokenValid(token);
            verify(jwtTokenGeneratorAdapter).extractCustomUserPrincipal(token);
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void doFilterInternal_shouldNotAuthenticateButContinueChainIfTokenIsInvalid() throws ServletException, IOException {
            String token = "invalid.jwt.token";
            request.addHeader("Authorization", "Bearer " + token);

            when(jwtTokenGeneratorAdapter.isTokenValid(token)).thenReturn(false);

            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(jwtTokenGeneratorAdapter).isTokenValid(token);
            verify(jwtTokenGeneratorAdapter, never()).extractCustomUserPrincipal(anyString());
            verify(filterChain).doFilter(request, response);
        }

        @Test
        void doFilterInternal_shouldClearContextAndContinueChainWhenExceptionIsThrown() throws ServletException, IOException {
            String token = "error.jwt.token";
            request.addHeader("Authorization", "Bearer " + token);

            SecurityContextHolder.getContext().setAuthentication(mock(Authentication.class));

            when(jwtTokenGeneratorAdapter.isTokenValid(token)).thenThrow(new RuntimeException("Unexpected error"));

            jwtAuthenticationFilter.doFilter(request, response, filterChain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(filterChain).doFilter(request, response);
        }
}