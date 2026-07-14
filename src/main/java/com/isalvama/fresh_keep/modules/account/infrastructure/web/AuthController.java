package com.isalvama.fresh_keep.modules.account.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.AuthenticationUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.request.RegisterUserAuthRequest;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationUseCase authenticationUseCase;

    @PostMapping("/user/register")
    public ResponseEntity<AuthResponseDto> registerUser(@Valid @RequestBody RegisterUserAuthRequest request) {
        AuthResponseDto response = authenticationUseCase.register(
                RegisterAccountCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.accountId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

}
