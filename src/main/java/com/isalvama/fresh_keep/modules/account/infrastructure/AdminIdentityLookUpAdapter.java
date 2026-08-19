package com.isalvama.fresh_keep.modules.account.infrastructure;

import com.isalvama.fresh_keep.modules.account.application.port.out.AdminIdentityLookUpPort;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.AdminProvisioningPendingException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminIdentityLookUpAdapter implements AdminIdentityLookUpPort {

    @Override
    public UUID getAdminIdByAccountId(AccountId accountId) {
        throw new AdminProvisioningPendingException("No mechanism exists yet to resolve an adminId.");
    }
}
