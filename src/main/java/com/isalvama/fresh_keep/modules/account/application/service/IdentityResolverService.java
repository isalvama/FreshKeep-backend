package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.port.out.AdminIdentityLookUpPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.UserIdentityLookUpPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.dto.ResolvedEntities;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.shared.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdentityResolverService {
    private final UserIdentityLookUpPort userIdentityLookUpPort;
    private final AdminIdentityLookUpPort adminIdentityLookUpPort;

    public ResolvedEntities resolveFor(Account account){
        String userId = null;
        String adminId = null;

        if (account.hasRole(Role.USER)){
            userId = userIdentityLookUpPort.getUserIdByAccountId(account.getId()).toString();
        }

        if (account.hasRole(Role.ADMIN)){
            adminId = adminIdentityLookUpPort.getAdminIdByAccountId(account.getId()).toString();
        }

        return ResolvedEntities.constitute(userId, adminId);
    }
}
