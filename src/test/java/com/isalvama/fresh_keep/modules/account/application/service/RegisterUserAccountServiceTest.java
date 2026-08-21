package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.command.RegisterUserAccountCommand;
import com.isalvama.fresh_keep.modules.account.application.port.out.AccountRepositoryPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.exception.AccountAlreadyExistsException;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.application.port.in.dto.response.AuthRegisterResult;
import com.isalvama.fresh_keep.modules.account.infrastructure.event.UserAccountEventPublisherAdapter;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserAccountServiceTest {

    private static final String email = "user@gmail.com";
    private static final String password = "userpassword";
    private static final RegisterUserAccountCommand command = new RegisterUserAccountCommand(email, password);
    private static final Account account = Account.reconstitute(AccountId.create(), Email.of(email), password, Set.of(Role.USER));

    @Mock
    private AccountRepositoryPort accountRepositoryPort;

    @Mock
    private PasswordHasherPort passwordHasherPort;

    @Mock
    private UserAccountEventPublisherAdapter accountEventPublisherAdapter;

    @InjectMocks
    private RegisterUserAccountService registerUserAccountService;

    @Test
    void shouldReturnAuthResponseSuccessfully(){
        String token = "token";
        Long exp = 7648427L;
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenReturn(password + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenAnswer(AdditionalAnswers.returnsFirstArg());

        AuthRegisterResult response = registerUserAccountService.execute(command);

        assertNotNull(response);
        assertNotNull(response.accountId());
        assertEquals(email, response.email());

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, times(1)).save(any(Account.class));
        verify(accountEventPublisherAdapter, times(1)).publish(any(UserAccountRegisteredEvent.class));
    }

    @Test
    void shouldNotExecuteIfAccountWithMatchingEmailAlreadyExists(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.of(Account.createUser(Email.of(email), password)));

        Exception exception = assertThrows(AccountAlreadyExistsException.class, () -> {registerUserAccountService.execute(command);});

        assertTrue(exception.getMessage().contains(email));
        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains("account with the email address"));

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, never()).hash(password);
        verify(accountRepositoryPort, never()).save(any(Account.class));
        verify(accountEventPublisherAdapter, never()).publish(any(UserAccountRegisteredEvent.class));
    }

    @Test
    void shouldNotContinueExecutingIfPasswordHashingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenThrow(new RuntimeException("Error in the hashing process"));

        assertThrows(RuntimeException.class, () -> {registerUserAccountService.execute(command);});

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, never()).save(any(Account.class));
        verify(accountEventPublisherAdapter, never()).publish(any(UserAccountRegisteredEvent.class));
    }


    @Test
    void shouldNotContinueExecutingIfPersistenceSavingProcessThrowsException(){
        when(accountRepositoryPort.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordHasherPort.hash(password)).thenReturn(password + " hashed");
        when(accountRepositoryPort.save(any(Account.class))).thenThrow(new RuntimeException("Error in the persistence process"));

        assertThrows(RuntimeException.class, () -> {registerUserAccountService.execute(command);});

        verify(accountRepositoryPort, times(1)).findByEmail(email);
        verify(passwordHasherPort, times(1)).hash(password);
        verify(accountRepositoryPort, times(1)).save(any(Account.class));
        verify(accountEventPublisherAdapter, never()).publish(any(UserAccountRegisteredEvent.class));
    }
}