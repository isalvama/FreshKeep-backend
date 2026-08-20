package com.isalvama.fresh_keep.modules.user.application.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.application.command.RegisterUserCommand;
import com.isalvama.fresh_keep.modules.user.application.port.out.UserRepositoryPort;
import com.isalvama.fresh_keep.modules.user.domain.exception.UserAlreadyExistsException;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setupLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger(RegisterUserService.class);

        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }
    private static final String EMAIL = "user@gmail.com";
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final User USER = User.create(AccountId.of(ACCOUNT_ID), Email.of(EMAIL));

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void shouldCallSaveRepositoryMethodSuccessfully (){
        Mockito.when((userRepositoryPort.findByAccountId(ACCOUNT_ID))).thenReturn(Optional.empty());
        Mockito.when((userRepositoryPort.findByEmail(EMAIL))).thenReturn(Optional.empty());

        registerUserService.execute(new RegisterUserCommand(ACCOUNT_ID.toString(), EMAIL));

        List<ILoggingEvent> logs = listAppender.list;

        assertEquals(0, logs.size());

        verify(userRepositoryPort, times(1)).findByAccountId(ACCOUNT_ID);
        verify(userRepositoryPort, times(1)).findByEmail(EMAIL);
        verify(userRepositoryPort, times(1)).save(any(User.class));
    }

    @Test
    void shouldNotCallPersistenceMethodWhenUserWithMatchingAccountIdExists (){
        Mockito.when((userRepositoryPort.findByAccountId(ACCOUNT_ID))).thenReturn(Optional.of(USER));

        registerUserService.execute(new RegisterUserCommand(ACCOUNT_ID.toString(), EMAIL));

        List<ILoggingEvent> logs = listAppender.list;

        assertEquals(1, logs.size());
        assertEquals(Level.ERROR, logs.getFirst().getLevel());
        assertTrue(logs.getFirst().getFormattedMessage().contains(ACCOUNT_ID.toString()));
        assertTrue(logs.getFirst().getFormattedMessage().contains("already exists"));

        verify(userRepositoryPort, times(1)).findByAccountId(ACCOUNT_ID);
        verify(userRepositoryPort, never()).findByEmail(any(String.class));
        verify(userRepositoryPort, never()).save(any(User.class));
    }

    @Test
    void shouldNotCallPersistenceMethodWhenUserWithMatchingEmailExists (){
        Mockito.when((userRepositoryPort.findByAccountId(ACCOUNT_ID))).thenReturn(Optional.empty());
        Mockito.when((userRepositoryPort.findByEmail(EMAIL))).thenReturn(Optional.of(USER));

        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {registerUserService.execute(new RegisterUserCommand(ACCOUNT_ID.toString(), EMAIL));
        });

        List<ILoggingEvent> logs = listAppender.list;

        assertEquals(0, logs.size());

        verify(userRepositoryPort, times(1)).findByAccountId(ACCOUNT_ID);
        verify(userRepositoryPort,  times(1)).findByEmail(EMAIL);
        verify(userRepositoryPort, never()).save(any(User.class));

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("already exists"));
        assertTrue(exception.getMessage().contains(EMAIL));
    }
}