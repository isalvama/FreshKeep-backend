package com.isalvama.fresh_keep.modules.account.infrastructure.bootstrap;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.exception.InvalidEmailException;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Provisions the first admin from {@code admin.bootstrap.email} / {@code admin.bootstrap.password}, since
 * {@code POST /api/v1/auth/register/admin} can only be called by an already-authenticated admin.
 * Idempotent: an existing admin is left untouched, and an existing USER account is promoted keeping its password.
 */
@Slf4j
@Component
public class AdminBootstrapRunner implements ApplicationRunner {
    static final String EMAIL_PROPERTY = "admin.bootstrap.email";
    static final String PASSWORD_PROPERTY = "admin.bootstrap.password";
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 20;

    private final AccountRepositoryPort accountRepositoryPort;
    private final RegisterAdminAccountUseCase registerAdminAccountUseCase;
    private final String email;
    private final String password;

    public AdminBootstrapRunner(AccountRepositoryPort accountRepositoryPort,
                                RegisterAdminAccountUseCase registerAdminAccountUseCase,
                                @Value("${" + EMAIL_PROPERTY + ":}") String email,
                                @Value("${" + PASSWORD_PROPERTY + ":}") String password) {
        this.accountRepositoryPort = accountRepositoryPort;
        this.registerAdminAccountUseCase = registerAdminAccountUseCase;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean emailMissing = email == null || email.isBlank();
        boolean passwordMissing = password == null || password.isBlank();

        if (emailMissing && passwordMissing) {
            log.info("Admin bootstrap skipped: {} and {} are not set.", EMAIL_PROPERTY, PASSWORD_PROPERTY);
            return;
        }
        if (emailMissing) throw new IllegalStateException("Admin bootstrap misconfigured: " + EMAIL_PROPERTY + " is not set but " + PASSWORD_PROPERTY + " is.");
        if (passwordMissing) throw new IllegalStateException("Admin bootstrap misconfigured: " + PASSWORD_PROPERTY + " is not set but " + EMAIL_PROPERTY + " is.");

        String normalizedEmail = validateEmail();
        validatePassword();

        Optional<Account> existingAccount = accountRepositoryPort.findByEmail(normalizedEmail);
        if (existingAccount.isPresent() && existingAccount.get().hasRole(Role.ADMIN)) {
            log.info("Admin bootstrap skipped: an admin account with the email {} already exists.", normalizedEmail);
            return;
        }

        try {
            registerAdminAccountUseCase.execute(RegisterAdminAccountCommand.builder()
                    .email(normalizedEmail)
                    .rawPassword(password)
                    .build());
        } catch (AccountAlreadyExistsException e) {
            // Another instance provisioned the same admin concurrently.
            log.info("Admin bootstrap skipped: an admin account with the email {} already exists.", normalizedEmail);
            return;
        }

        if (existingAccount.isPresent()) {
            log.warn("Admin bootstrap promoted the existing account {} to ADMIN. It keeps its current password; {} was not applied.",
                    normalizedEmail, PASSWORD_PROPERTY);
        } else {
            log.info("Admin bootstrap created the admin account {}.", normalizedEmail);
        }
    }

    private String validateEmail() {
        try {
            return Email.of(email).value();
        } catch (InvalidEmailException e) {
            throw new IllegalStateException("Admin bootstrap misconfigured: " + EMAIL_PROPERTY + " is invalid. " + e.getMessage());
        }
    }

    private void validatePassword() {
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalStateException("Admin bootstrap misconfigured: " + PASSWORD_PROPERTY + " must be between "
                    + PASSWORD_MIN_LENGTH + " and " + PASSWORD_MAX_LENGTH + " characters long.");
        }
    }
}
