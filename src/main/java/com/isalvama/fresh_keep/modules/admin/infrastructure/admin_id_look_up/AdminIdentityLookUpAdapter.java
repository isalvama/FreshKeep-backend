package com.isalvama.fresh_keep.modules.admin.infrastructure.admin_id_look_up;

import com.isalvama.fresh_keep.modules.account.application.port.out.AdminIdentityLookUpPort;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.AdminProvisioningPendingException;
import com.isalvama.fresh_keep.modules.admin.application.port.out.AdminRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminIdentityLookUpAdapter implements AdminIdentityLookUpPort {
    private final AdminRepositoryPort adminRepositoryPort;

    @Override
    public UUID getAdminIdByAccountId(AccountId accountId) {
        return adminRepositoryPort.findByAccountId(accountId.value())
                .map(a -> a.getId().value())
                .orElseThrow(() -> new AdminProvisioningPendingException("An Admin with the accountId " + accountId.toString() + " has not been created yet."));
    }
}
