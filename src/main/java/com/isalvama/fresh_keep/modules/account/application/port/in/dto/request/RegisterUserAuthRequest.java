package com.isalvama.fresh_keep.modules.account.application.port.in.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserAuthRequest(
        @NotBlank @Email String email,
        @NotBlank @Min(8) @Max(20) String password
) {}
