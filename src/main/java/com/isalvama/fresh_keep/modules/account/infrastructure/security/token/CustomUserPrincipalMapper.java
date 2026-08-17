package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.shared.domain.Role;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CustomUserPrincipalMapper {

    public CustomUserPrincipal fromDomain (Account account) {
        return new CustomUserPrincipal(
                account.getId().toString(),
                account.getEmail().toString(),
                account.getPasswordHash(),
                account.getRoles().stream().map(Enum::toString).toList()
        );
    }

    public Account fromUserPrincipal (CustomUserPrincipal customUser){
        return Account.reconstitute(
               AccountId.from(customUser.id()),
               Email.of(customUser.email()),
                customUser.passwordHash(),
                customUser.roles().stream().map(Role::valueOf).collect(Collectors.toSet())
        );
    }
}
