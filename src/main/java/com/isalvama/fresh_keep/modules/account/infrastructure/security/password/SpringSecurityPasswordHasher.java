package com.isalvama.fresh_keep.modules.account.infrastructure.security.password;

import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class SpringSecurityPasswordHasher implements PasswordHasherPort {
    private final PasswordEncoder passwordEncoder;

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
