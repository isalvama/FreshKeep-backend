package com.isalvama.fresh_keep.modules.account.infrastructure.security.password;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityPasswordHasherTest {

    private SpringSecurityPasswordHasher springSecurityPasswordHasher;

    @BeforeEach
    void setUp(){
        springSecurityPasswordHasher = new SpringSecurityPasswordHasher(new BCryptPasswordEncoder());
    }

    @Test
    void hash() {
        String rawPassword = "raw password";
        String hashedPassword = springSecurityPasswordHasher.hash("raw password");
        assertNotEquals(rawPassword, hashedPassword);
        assertTrue(new BCryptPasswordEncoder().matches(rawPassword, hashedPassword));
    }

    @Test
    void shouldGenerateDifferentHashesForSamePassword() {
        String rawPassword = "mySecretPassword123";

        String hash1 = springSecurityPasswordHasher.hash(rawPassword);
        String hash2 = springSecurityPasswordHasher.hash(rawPassword);

        assertNotEquals(hash1, hash2);
    }
    @Test
    void shouldMatchValidPassword() {
        String rawPassword = "mySecretPassword123";
        String hashed = springSecurityPasswordHasher.hash(rawPassword);

        boolean isMatch = springSecurityPasswordHasher.matches(rawPassword, hashed);

        assertTrue(isMatch);
    }

    @Test
    void shouldNotMatchInvalidPassword() {
        String correctPassword = "mySecretPassword123";
        String wrongPassword = "wrongPassword456";
        String hashed = springSecurityPasswordHasher.hash(correctPassword);

        boolean isMatch = springSecurityPasswordHasher.matches(wrongPassword, hashed);

        assertFalse(isMatch);
    }
}