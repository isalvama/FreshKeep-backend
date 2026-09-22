package com.isalvama.fresh_keep.modules.admin.application.command;

import lombok.Builder;

@Builder
public record RegisterAdminCommand(
        String accountId,
        String email
) {
}
