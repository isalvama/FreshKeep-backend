package com.isalvama.fresh_keep.modules.account.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.request.RegisterUserAuthRequest;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterUserAccountUseCase registerUserAccountUseCase;
    private final RegisterAdminAccountUseCase registerAdminAccountUseCase;


    @PostMapping("/register/user")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody RegisterUserAuthRequest request) {
        AuthResponseDto response = registerUserAccountUseCase.execute(
                RegisterUserAccountCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(response.accountId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDto> registerAdmin(@Valid @RequestBody RegisterUserAuthRequest request) {
        AuthResponseDto response = registerAdminAccountUseCase.execute(
                RegisterAdminAccountCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/admins/{id}")
                .buildAndExpand(response.accountId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

}
