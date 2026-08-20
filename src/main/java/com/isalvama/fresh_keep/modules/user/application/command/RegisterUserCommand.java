package com.isalvama.fresh_keep.modules.user.application.command;

import lombok.Builder;

@Builder
public record RegisterUserCommand (
        String accountId,
        String email
){
}
