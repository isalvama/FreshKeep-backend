package com.isalvama.fresh_keep.modules.account.infrastructure.web;

import com.isalvama.fresh_keep.modules.account.application.command.LoginCommand;
import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.LoginUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthJwtResult;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.mapper.AuthResponseMapper;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request.AuthRequest;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthJwtResponse;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.response.AuthRegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Auth", description = "Account registration, login, and JWT issuance")
public class AuthController {

    private final RegisterUserAccountUseCase registerUserAccountUseCase;
    private final RegisterAdminAccountUseCase registerAdminAccountUseCase;
    private final LoginUseCase loginUseCase;

    @PostMapping("/register/user")
    @Operation(
            summary = "Register a new user account",
            description = "Creates an account with the USER role. Returns only accountId/email — " +
                    "no JWT. Call POST /login afterward to obtain a session token."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created",
                    content = @Content(schema = @Schema(implementation = AuthRegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the request body is malformed/missing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "An account with this email and the USER role already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure: looking up or saving the " +
                    "account, or looking up or saving its provisioned User record",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AuthRegisterResponse> registerUser(@Valid @RequestBody AuthRequest request) {
        AuthRegisterResult result = registerUserAccountUseCase.execute(
                RegisterUserAccountCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        AuthRegisterResponse response = AuthResponseMapper.toResponse(result);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{id}")
                .buildAndExpand(response.accountId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Register a new admin account (ADMIN-only)",
            description = "Requires a caller already authenticated with ROLE_ADMIN. Grants the ADMIN role instead " +
                    "of USER. If the email already belongs to a USER account, that account is promoted to ADMIN " +
                    "and keeps its existing password (the password in the request is ignored). The very first " +
                    "admin can't be created through this endpoint — it's provisioned at startup via " +
                    "ADMIN_BOOTSTRAP_EMAIL/ADMIN_BOOTSTRAP_PASSWORD. Returns only accountId/email — no JWT."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created or promoted",
                    content = @Content(schema = @Schema(implementation = AuthRegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the request body is malformed/missing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No/invalid/expired bearer token",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Authenticated, but the caller does not have the ADMIN role",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "An ADMIN account with this email already exists",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected persistence failure: looking up or saving the " +
                    "account, or looking up or saving its provisioned Admin record",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AuthRegisterResponse> registerAdmin(@Valid @RequestBody AuthRequest request) {
        AuthRegisterResult result = registerAdminAccountUseCase.execute(
                RegisterAdminAccountCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        AuthRegisterResponse response = AuthResponseMapper.toResponse(result);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/admins/{id}")
                .buildAndExpand(response.accountId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Log in and obtain a JWT",
            description = "Returns a bearer JWT whose claims carry roles (ROLE_-prefixed), accountId, and, when " +
                    "applicable, userId/adminId resolved from the account's provisioned domain identities. " +
                    "expiresIn is the raw jwt.expiration value in milliseconds, not a computed remaining time."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login succeeded",
                    content = @Content(schema = @Schema(implementation = AuthJwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed, or the request body is malformed/missing",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "Wrong email or password",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Account exists but is disabled",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Unexpected failure: when principal is not of type CustomUserPrincipal, or the persistence of a login account failed, or when userId and/or adminId are null when generating the jwt token, when user/admin account with the corresponding account id has not been created, or when the retrieval of the user/admin data has failed",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<AuthJwtResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthJwtResult result = loginUseCase.execute(
                LoginCommand.builder()
                        .email(request.email())
                        .rawPassword(request.password())
                        .build()
        );

        return ResponseEntity.ok(AuthResponseMapper.toResponse(result));
    }

}
