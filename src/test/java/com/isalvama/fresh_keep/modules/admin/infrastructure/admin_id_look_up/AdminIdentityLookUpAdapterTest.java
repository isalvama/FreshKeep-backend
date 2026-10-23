package com.isalvama.fresh_keep.modules.admin.infrastructure.admin_id_look_up;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.AdminProvisioningPendingException;
import com.isalvama.fresh_keep.modules.admin.application.port.out.AdminRepositoryPort;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.AdminId;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminIdentityLookUpAdapterTest {

    private static final AccountId ACCOUNT_ID = AccountId.create();

    @Mock
    private AdminRepositoryPort adminRepositoryPort;

    @InjectMocks
    private AdminIdentityLookUpAdapter adminIdentityLookUpAdapter;

    @Test
    void shouldReturnAdminIdWhenAdminExistsForAccount() {
        UUID adminId = UUID.randomUUID();
        Admin admin = Admin.reconstitute(AdminId.of(adminId), ACCOUNT_ID, Email.of("admin@gmail.com"));
        when(adminRepositoryPort.findByAccountId(ACCOUNT_ID.value())).thenReturn(Optional.of(admin));

        assertEquals(adminId, adminIdentityLookUpAdapter.getAdminIdByAccountId(ACCOUNT_ID));
    }

    @Test
    void shouldThrowAdminProvisioningPendingExceptionWhenNoAdminExistsForAccount() {
        when(adminRepositoryPort.findByAccountId(ACCOUNT_ID.value())).thenReturn(Optional.empty());

        assertThrows(AdminProvisioningPendingException.class,
                () -> adminIdentityLookUpAdapter.getAdminIdByAccountId(ACCOUNT_ID));
    }
}
