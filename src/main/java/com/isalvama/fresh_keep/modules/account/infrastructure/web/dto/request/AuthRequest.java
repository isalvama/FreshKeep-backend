package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request;

import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {}
