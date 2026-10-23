package com.isalvama.fresh_keep.modules.account.infrastructure.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorsConfigurationTest {

    private static org.springframework.web.cors.CorsConfiguration configurationFor(List<String> allowedOrigins) {
        CorsConfigurationSource source = new CorsConfiguration().corsConfigurationSource(allowedOrigins);
        return source.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/v1/admin/users"));
    }

    @Test
    void shouldRejectEveryOriginWhenNoOriginsAreConfigured() {
        org.springframework.web.cors.CorsConfiguration configuration = configurationFor(List.of());

        assertNull(configuration.checkOrigin("http://localhost:5050"));
    }

    @Test
    void shouldIgnoreBlankEntriesAndTrimConfiguredOrigins() {
        org.springframework.web.cors.CorsConfiguration configuration =
                configurationFor(List.of(" http://localhost:5050 ", ""));

        assertEquals(List.of("http://localhost:5050"), configuration.getAllowedOrigins());
        assertEquals("http://localhost:5050", configuration.checkOrigin("http://localhost:5050"));
        assertNull(configuration.checkOrigin("http://evil.example.com"));
    }
}
