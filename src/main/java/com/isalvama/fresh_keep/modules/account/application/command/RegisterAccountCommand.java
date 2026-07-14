package com.isalvama.fresh_keep.modules.account.application.command;

import com.isalvama.fresh_keep.shared.domain.Role;
import lombok.Builder;

import java.util.Set;

@Builder
public record RegisterAccountCommand(
        String email,
        String rawPassword
) {
    public Set<Role> roles() {
        return Set.of(Role.USER);
    }

}
