package com.isalvama.fresh_keep.modules.admin.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.AdminId;
import com.isalvama.fresh_keep.modules.user.domain.exception.InvalidUserException;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AdminTest {

    private static final Email EMAIL = Email.of("admin@example.com");

    @Test
    void create_shouldCreateAdminWithGeneratedId() {
        AccountId accountId = AccountId.create();

        Admin admin = Admin.create(accountId, EMAIL);

        assertNotNull(admin.getId());
        assertEquals(accountId, admin.getAccountId());
        assertEquals(EMAIL, admin.getEmail());
    }

    @Test
    void create_shouldRejectNullArguments() {
        AccountId accountId = AccountId.create();

        assertThrows(InvalidUserException.class, () -> Admin.create(null, EMAIL));
        assertThrows(InvalidUserException.class, () -> Admin.create(accountId, null));
    }

    @Test
    void reconstitute_shouldRestoreAdmin() {
        AdminId id = AdminId.create();
        AccountId accountId = AccountId.create();

        Admin admin = Admin.reconstitute(id, accountId, EMAIL);

        assertEquals(id, admin.getId());
        assertEquals(accountId, admin.getAccountId());
        assertEquals(EMAIL, admin.getEmail());
    }

    @Test
    void reconstitute_shouldRejectNullArguments() {
        AdminId id = AdminId.create();
        AccountId accountId = AccountId.create();

        assertThrows(InvalidUserException.class, () -> Admin.reconstitute(null, accountId, EMAIL));
        assertThrows(InvalidUserException.class, () -> Admin.reconstitute(id, null, EMAIL));
        assertThrows(InvalidUserException.class, () -> Admin.reconstitute(id, accountId, null));
    }
}
