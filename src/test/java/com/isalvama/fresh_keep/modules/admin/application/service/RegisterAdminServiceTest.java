package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.application.command.RegisterAdminCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.out.AdminRepositoryPort;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.user.domain.exception.UserAlreadyExistsException;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterAdminServiceTest {

    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String EMAIL = "admin@gmail.com";
    private static final RegisterAdminCommand COMMAND =
            new RegisterAdminCommand(ACCOUNT_ID.toString(), EMAIL);

    @Mock
    private AdminRepositoryPort adminRepositoryPort;

    @InjectMocks
    private RegisterAdminService registerAdminService;

    @Test
    void shouldRegisterAdminWhenAccountIdAndEmailAreAvailable() {
        when(adminRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
        when(adminRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

        registerAdminService.execute(COMMAND);

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepositoryPort).save(adminCaptor.capture());
        assertEquals(ACCOUNT_ID, adminCaptor.getValue().getAccountId().value());
        assertEquals(EMAIL, adminCaptor.getValue().getEmail().value());
    }

    @Test
    void shouldSkipRegistrationWhenAdminWithAccountIdAlreadyExists() {
        Admin existingAdmin = Admin.create(AccountId.of(ACCOUNT_ID), Email.of(EMAIL));
        when(adminRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.of(existingAdmin));

        registerAdminService.execute(COMMAND);

        verify(adminRepositoryPort, never()).findByEmail(any(String.class));
        verify(adminRepositoryPort, never()).save(any(Admin.class));
    }

    @Test
    void shouldFailWhenAdminWithEmailAlreadyExists() {
        Admin existingAdmin = Admin.create(AccountId.create(), Email.of(EMAIL));
        when(adminRepositoryPort.findByAccountId(ACCOUNT_ID)).thenReturn(Optional.empty());
        when(adminRepositoryPort.findByEmail(EMAIL)).thenReturn(Optional.of(existingAdmin));

        assertThrows(UserAlreadyExistsException.class,
                () -> registerAdminService.execute(COMMAND));

        verify(adminRepositoryPort, never()).save(any(Admin.class));
    }
}
