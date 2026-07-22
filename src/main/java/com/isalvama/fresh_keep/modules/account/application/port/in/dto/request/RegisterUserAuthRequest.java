package com.isalvama.fresh_keep.modules.account.application.port.in.dto.request;

import jakarta.validation.constraints.*;

public record RegisterUserAuthRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 20) String password
) {}
