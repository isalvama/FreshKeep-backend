package com.isalvama.fresh_keep.modules.account.application.command;

import lombok.Builder;

@Builder
public record LoginCommand(
        String email,
        String rawPassword
) {
}
