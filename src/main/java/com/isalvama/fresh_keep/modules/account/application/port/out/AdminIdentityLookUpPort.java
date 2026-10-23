package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;

import java.util.UUID;

public interface AdminIdentityLookUpPort {
    UUID getAdminIdByAccountId(AccountId accountId);
}
