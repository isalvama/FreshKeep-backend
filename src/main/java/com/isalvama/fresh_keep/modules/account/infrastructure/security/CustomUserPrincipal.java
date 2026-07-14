package com.isalvama.fresh_keep.modules.account.infrastructure.security;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserPrincipal implements UserDetails {
    @Getter
    private final String id;
    private final String email;
    private final String passwordHash;

    public CustomUserPrincipal(String id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public static CustomUserPrincipal fromDomain (Account account){
        return new CustomUserPrincipal(
                account.getId().toString(),
                account.getEmail().toString(),
                account.getPasswordHash()
        );
    }
}
