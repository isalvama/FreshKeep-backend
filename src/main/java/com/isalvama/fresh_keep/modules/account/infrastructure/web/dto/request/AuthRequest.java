package com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record AuthRequest(
        @NotBlank
        @Email
        @Schema(description = "Account email", example = "user@example.com")
        String email,

        @NotBlank
        @Size(min = 8, max = 20)
        @Schema(description = "Account password, 8-20 characters", example = "P@ssw0rd1")
        String password
) {}
