package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account.mapper;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account.JpaAccountEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AccountMapper {
    public Account toDomain(JpaAccountEntity account){
        return Account.reconstitute(
                AccountId.of(account.getId()),
                Email.of(account.getEmail()),
                account.getPasswordHash(),
                account.getRole()
        );
    }

    public JpaAccountEntity toEntity(Account account){
            if (account == null) return null;

        return JpaAccountEntity.builder()
                .id(account.getId().value())
                .email(account.getEmail().toString())
                .passwordHash(account.getPasswordHash())
                .role(account.getRole())
                .build();
    }
}
