package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterUserAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.event.AccountEventPublisherAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthenticationAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.JpaSpaceRepositoryAdapter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "jwt.secret=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "JWT_SECRET=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "application.security.jwt.secret-key=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "application.security.jwt.expiration=3600000",
        "application.security.jwt.refresh-token.expiration=86400000",
        "cloudinary.cloud_name=test-cloud",
        "cloudinary.api_key=test-api-key",
        "cloudinary.api_secret=test-api-secret"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
    class RegisterUserAccountTransactionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private RegisterUserAccountUseCase registerUserAccountService;

    @Autowired
    private AccountRepositoryPort accountRepositoryPort;

    @MockitoBean
    private PasswordHasherPort passwordHasherPort;

    @MockitoBean
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @MockitoBean
    private AccountEventPublisherAdapter accountEventPublisherAdapter;

    @MockitoBean
    private AuthenticationAdapter authenticationAdapter;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaSpaceRepositoryAdapter jpaSpaceRepositoryAdapter;

    @MockitoBean
    private SpaceMapper spaceMapper;

    @Test
        void shouldRollbackDatabaseIfEventPublishingFails() {
            String email = "test@email.com";
            when(passwordHasherPort.hash(any())).thenReturn("hashed_password");
            doThrow(new RuntimeException("Messaging system down"))
                    .when(accountEventPublisherAdapter).publish(any(UserAccountRegisteredEvent.class));
            RegisterUserAccountCommand command = new RegisterUserAccountCommand(
                    email, "password123"
            );

            assertThrows(RuntimeException.class, () -> {
                registerUserAccountService.execute(command);
            });

            Optional<Account> savedAccount = accountRepositoryPort.findByEmail(email);
            assertTrue(savedAccount.isEmpty(), "The user account should have not been persisted due to the rollback");
        }

}
