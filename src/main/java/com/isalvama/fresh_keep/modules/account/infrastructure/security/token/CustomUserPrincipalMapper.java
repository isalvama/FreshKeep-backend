package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import org.springframework.stereotype.Component;

@Component
public class CustomUserPrincipalMapper {

    public CustomUserPrincipal fromDomain (Account account){
        return new CustomUserPrincipal(
                account.getId().toString(),
                account.getEmail().toString(),
                account.getPasswordHash(),
                account.getRole().toString()
        );
    }
}
