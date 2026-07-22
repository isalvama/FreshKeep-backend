package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class JwtTokenGeneratorAdapterTest {

    private final static String USER = "USER";
    private final static String EMAIL = "email@email.com";
    private final static String PASSWORD = "random password";
    private final static String BASE64_SECRET = "M2FmYjU3ZGE0YmU3NGYwYmE0YmU3NGYwYmE0YmU3NGYwYmE0YmU3NGYwYmE0YmU3NGYwYmE0YmU3NGYwYmE0YmU3NA==";
    private final static long EXPIRATION = 3600000L;

    private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;
    private Account account;
    private String token;

    @BeforeEach
    void setUp() {
        jwtTokenGeneratorAdapter = new JwtTokenGeneratorAdapter(
                new CustomUserPrincipalMapper(),
                BASE64_SECRET,
                EXPIRATION
        );
        account = Account.createUser(
                Email.of(EMAIL),
                PASSWORD
        );
        token = jwtTokenGeneratorAdapter.generateToken(account).token();
    }

    @Test
    void generateToken_shouldContainThreePartsAndMustNotShowRawAccountInfo() {
        List<String> tokenParts = Arrays.stream(token.split("\\.")).toList();

        assertThat(token)
                .isNotNull()
                .containsPattern("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+$");
        assertEquals(3, tokenParts.size());
        assertFalse(token.contains(USER));
        assertFalse(token.contains(EMAIL));
        assertFalse(token.contains(account.getId().toString()));
    }

    @Test
    void extractCustomUserPrincipal_shouldContainRolesEmailId() {
        CustomUserPrincipal userPrincipal = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(token);
        assertEquals(userPrincipal.id(), account.getId().toString());
        assertEquals(userPrincipal.getUsername(), account.getEmail().toString());
        assertNull(userPrincipal.passwordHash());

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        assertThat(authorities)
                .hasSize(1)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER")
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void generateToken_with_multiple_roles() {
        Account adminAccount = Account.createAdmin(
                Email.of("admin@test.com"), PASSWORD
        );

        String token = jwtTokenGeneratorAdapter.generateToken(adminAccount).token();
        CustomUserPrincipal extracted = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(token);

        assertThat(extracted.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN")
                .doesNotContain("ROLE_USER");
    }

    @Test
    void extractClaim_success() {
        String subject = jwtTokenGeneratorAdapter.extractClaim(token, Claims::getSubject);
        Date expiration = jwtTokenGeneratorAdapter.extractClaim(token, Claims::getExpiration);

        assertThat(subject).isEqualTo(EMAIL);
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void isTokenValid_validation() {
        assertTrue(jwtTokenGeneratorAdapter.isTokenValid(token));

        String tamperedToken = token.substring(0, token.length() - 1) + (token.endsWith("a") ? "b" : "a");
        assertFalse(jwtTokenGeneratorAdapter.isTokenValid(tamperedToken));

        assertFalse(jwtTokenGeneratorAdapter.isTokenValid("invalid.token.structure"));
    }

    @Test
    void isTokenValid_newTokenShouldBeFalse() {
        assertTrue(jwtTokenGeneratorAdapter.isTokenValid(token));
    }
}

