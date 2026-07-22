package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserPrincipalTest {

    private final static String ADMIN = "ADMIN";
    private final static String USER = "USER";
    private final static String EMAIL = "email@email.com";
    private final static String ID = UUID.randomUUID().toString();
    private final static String RANDOM_PASSWORD = "random password";


    @Test
    void getAuthorities_shouldReturnListOfAdminRoleWithROLEPrefix() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(
                ID,
                EMAIL,
                RANDOM_PASSWORD,
                List.of(ADMIN)
                );

        Collection<? extends GrantedAuthority> auths = customUserPrincipal.getAuthorities();

        List<String> authStrings = auths.stream().map(GrantedAuthority::getAuthority).toList();
        Assertions.assertEquals(1, authStrings.size());
        Assertions.assertTrue(authStrings.contains("ROLE_ADMIN"));
    }
    @Test
    void getAuthorities_shouldReturnListOfUserRolesWithROLEPrefix() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(
                ID,
                EMAIL,
                RANDOM_PASSWORD,
                List.of(USER)
        );

        Collection<? extends GrantedAuthority> auths = customUserPrincipal.getAuthorities();

        List<String> authStrings = auths.stream().map(GrantedAuthority::getAuthority).toList();
        Assertions.assertEquals(1, authStrings.size());
        Assertions.assertTrue(authStrings.contains("ROLE_USER"));
    }

    @Test
    void getAuthorities_shouldReturnListOfUserAndAdminRolesWithROLEPrefix() {
        CustomUserPrincipal customUserPrincipal = new CustomUserPrincipal(
                ID,
                EMAIL,
                RANDOM_PASSWORD,
                List.of(USER, ADMIN)
        );

        Collection<? extends GrantedAuthority> auths = customUserPrincipal.getAuthorities();

        List<String> authStrings = auths.stream().map(GrantedAuthority::getAuthority).toList();
        Assertions.assertEquals(2, authStrings.size());
        Assertions.assertTrue(authStrings.contains("ROLE_USER"));
        Assertions.assertTrue(authStrings.contains("ROLE_ADMIN"));
    }

    @Test
    void getAuthorities_shouldReturnEmptyListWhenNoRoles() {
        CustomUserPrincipal principal = new CustomUserPrincipal(ID, EMAIL, RANDOM_PASSWORD, List.of());

        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();

        assertTrue(authorities.isEmpty());
    }
}