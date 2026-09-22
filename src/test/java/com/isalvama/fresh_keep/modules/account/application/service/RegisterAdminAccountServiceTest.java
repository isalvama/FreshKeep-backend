package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterAdminAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEventPublisher;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterAdminAccountServiceTest {

    private static final String EMAIL = "admin@gmail.com";
    private static final String PASSWORD = "adminpassword";
    private static final RegisterAdminAccountCommand COMMAND = new RegisterAdminAccountCommand(EMAIL, PASSWORD);

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private AdminAccountRegisteredEventPublisher accountEventPublisher;

    @InjectMocks
    private RegisterAdminAccountService registerAdminAccountService;

    @Test
    void shouldRegisterNewAdminAccountSuccessfully(){
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(PASSWORD)).thenReturn(PASSWORD + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        AuthRegisterResult response = registerAdminAccountService.execute(COMMAND);

        assertNotNull(response);
        assertNotNull(response.accountId());
        assertEquals(EMAIL, response.email());

        verify(accountRepositoryPort).findByEmail(EMAIL);
        verify(passwordHasherPort).hash(PASSWORD);
        verify(accountRepositoryPort).save(any(Account.class));
        verify(accountEventPublisher).publish(any(AdminAccountRegisteredEvent.class));
    }

    @Test
    void shouldNotRegisterWhenAdminAccountWithMatchingEmailAlreadyExists(){
        when(accountRepositoryPort.findByEmail(EMAIL))
                .thenReturn(Optional.of(Account.createAdmin(Email.of(EMAIL), PASSWORD)));

        Exception exception = assertThrows(AccountAlreadyExistsException.class, () -> registerAdminAccountService.execute(COMMAND));

        assertTrue(exception.getMessage().contains(EMAIL));
        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains("account with the email address"));

        verify(accountRepositoryPort).findByEmail(EMAIL);
        verify(passwordHasherPort, never()).hash(PASSWORD);
        verify(accountRepositoryPort, never()).save(any(Account.class));
        verify(accountEventPublisher, never()).publish(any(AdminAccountRegisteredEvent.class));
    }

    @Test
    void shouldAddAdminRoleToExistingNonAdminAccount(){
        Account existingAccount = Account.createUser(Email.of(EMAIL), PASSWORD);
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(existingAccount));
        when(accountRepositoryPort.save(any(Account.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        AuthRegisterResult response = registerAdminAccountService.execute(COMMAND);

        assertEquals(existingAccount.getId().toString(), response.accountId());
        assertEquals(EMAIL, response.email());
        assertTrue(existingAccount.hasRole(Role.ADMIN));
        verify(accountRepositoryPort).save(existingAccount);
        verify(passwordHasherPort, never()).hash(any());
        verify(accountEventPublisher).publish(any(AdminAccountRegisteredEvent.class));
    }

    @Test
    void shouldNotContinueExecutingIfPasswordHashingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(PASSWORD)).thenThrow(new RuntimeException("Error in the hashing process"));

        assertThrows(RuntimeException.class, () -> registerAdminAccountService.execute(COMMAND));

        verify(accountRepositoryPort).findByEmail(EMAIL);
        verify(passwordHasherPort).hash(PASSWORD);
        verify(accountRepositoryPort, never()).save(any(Account.class));
        verify(accountEventPublisher, never()).publish(any(AdminAccountRegisteredEvent.class));
    }

    @Test
    void shouldNotContinueExecutingIfPersistenceSavingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(PASSWORD)).thenReturn(PASSWORD + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenThrow(new RuntimeException("Error in the persistence process"));

        assertThrows(RuntimeException.class, () -> registerAdminAccountService.execute(COMMAND));

        verify(accountRepositoryPort).findByEmail(EMAIL);
        verify(passwordHasherPort).hash(PASSWORD);
        verify(accountRepositoryPort).save(any(Account.class));
        verify(accountEventPublisher, never()).publish(any(AdminAccountRegisteredEvent.class));
    }
}
