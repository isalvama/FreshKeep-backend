package com.isalvama.fresh_keep.modules.account.infrastructure.bootstrap;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.in.RegisterAdminAccountUseCase;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class AdminBootstrapRunnerTest {

    private static final String EMAIL = "bootstrap@admin.com";
    private static final String PASSWORD = "S3cretBootPwd";

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private RegisterAdminAccountUseCase registerAdminAccountUseCase;

    @Mock
    private PlatformTransactionManager transactionManager;

    private AdminBootstrapRunner runner(String email, String password) {
        return new AdminBootstrapRunner(accountRepositoryPort, registerAdminAccountUseCase,
                new TransactionTemplate(transactionManager), email, password);
    }

    @Test
    void shouldSkipWithoutCallingUseCaseWhenPropertiesAreBlank(CapturedOutput output) {
        runner("", " ").run(null);

        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
        assertThat(output).contains("Admin bootstrap skipped");
    }

    @Test
    void shouldSkipWhenAccountIsAlreadyAdmin() {
        when(accountRepositoryPort.findByEmail(EMAIL))
                .thenReturn(Optional.of(Account.createAdmin(Email.of(EMAIL), "hash")));

        runner(EMAIL, PASSWORD).run(null);

        verify(registerAdminAccountUseCase, never()).execute(any());
    }

    @Test
    void shouldPromoteExistingUserAccountAndWarnThatPasswordIsKept(CapturedOutput output) {
        when(accountRepositoryPort.findByEmail(EMAIL))
                .thenReturn(Optional.of(Account.createUser(Email.of(EMAIL), "hash")));

        runner(EMAIL, PASSWORD).run(null);

        verify(registerAdminAccountUseCase).execute(any());
        assertThat(output).contains("WARN").contains("keeps its current password");
        assertThat(output).doesNotContain(PASSWORD);
    }

    @Test
    void shouldCreateAdminWhenNoAccountExists(CapturedOutput output) {
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        runner(" Bootstrap@Admin.com ", PASSWORD).run(null);

        ArgumentCaptor<RegisterAdminAccountCommand> commandCaptor = ArgumentCaptor.forClass(RegisterAdminAccountCommand.class);
        verify(registerAdminAccountUseCase).execute(commandCaptor.capture());
        assertEquals(EMAIL, commandCaptor.getValue().email());
        assertEquals(PASSWORD, commandCaptor.getValue().rawPassword());
        assertThat(output).doesNotContain(PASSWORD);
    }

    @Test
    void shouldNotFailWhenAdminWasCreatedConcurrently() {
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(registerAdminAccountUseCase.execute(any()))
                .thenThrow(new AccountAlreadyExistsException("already exists"));

        assertDoesNotThrow(() -> runner(EMAIL, PASSWORD).run(null));
    }

    @Test
    void shouldFailNamingEmailPropertyWhenOnlyPasswordIsSet() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> runner("", PASSWORD).run(null));

        assertThat(exception.getMessage()).contains("admin.bootstrap.email").doesNotContain(PASSWORD);
        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
    }

    @Test
    void shouldFailNamingPasswordPropertyWhenOnlyEmailIsSet() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> runner(EMAIL, null).run(null));

        assertThat(exception.getMessage()).contains("admin.bootstrap.password");
        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
    }

    @Test
    void shouldFailNamingEmailPropertyWhenEmailIsMalformed() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> runner("not-an-email", PASSWORD).run(null));

        assertThat(exception.getMessage()).contains("admin.bootstrap.email").doesNotContain(PASSWORD);
        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
    }

    @Test
    void shouldFailNamingPasswordPropertyWhenPasswordIsTooShort() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> runner(EMAIL, "short").run(null));

        assertThat(exception.getMessage()).contains("admin.bootstrap.password").doesNotContain("short");
        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
    }

    @Test
    void shouldFailNamingPasswordPropertyWhenPasswordIsTooLong() {
        String longPassword = "a".repeat(21);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> runner(EMAIL, longPassword).run(null));

        assertThat(exception.getMessage()).contains("admin.bootstrap.password").doesNotContain(longPassword);
        verifyNoInteractions(accountRepositoryPort, registerAdminAccountUseCase);
    }
}
