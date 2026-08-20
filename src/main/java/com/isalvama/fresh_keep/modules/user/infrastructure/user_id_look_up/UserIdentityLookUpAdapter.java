package com.isalvama.fresh_keep.modules.user.infrastructure.user_id_look_up;

import com.isalvama.fresh_keep.modules.account.application.port.out.UserIdentityLookUpPort;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.application.port.out.UserRepositoryPort;
import com.isalvama.fresh_keep.modules.user.infrastructure.user_id_look_up.exception.UserProvisioningPendingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserIdentityLookUpAdapter implements UserIdentityLookUpPort {
    private final UserRepositoryPort userRepositoryPort;
    @Override
    public UUID getUserIdByAccountId(AccountId accountId) {
        return userRepositoryPort.findByAccountId(accountId.value())
                .map(u -> u.getId().value())
                .orElseThrow(() -> new UserProvisioningPendingException("A User account with the accountId " + accountId.toString() + " has not been created yet."));
    }
}
