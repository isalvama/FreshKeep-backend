package com.isalvama.fresh_keep.modules.account.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountException;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.shared.domain.Role;
import lombok.Getter;

@Getter
public class Account {
    private final AccountId id;
    private final Email email;
    private final String passwordHash;
    private final Role role;

    private Account(AccountId id, Email email, String passwordHash, Role role){
        this.id = validateNotNull(id, "id");
        this.email = validateNotNull(email, "email");
        this.passwordHash = validateNotNull(passwordHash, "passwordHash");
        this.role = validateNotNull(role, "role");
    }

    public static Account createUser(Email email, String passwordHash){
        return new Account(
                AccountId.generate(),
                email,
                passwordHash,
                Role.USER
        );
    }

    public static Account createAdmin (AccountId id, Email email, String passwordHash){
        return new Account(
                id,
                email,
                passwordHash,
                Role.ADMIN
        );
    }

    public static Account reconstitute (AccountId id, Email email, String passwordHash, Role role){
        return new Account(
                id,
                email,
                passwordHash,
                role
        );
    }

    private static <T> T validateNotNull(T obj, String fieldName) {
        if (obj == null)
            throw new InvalidAccountException(fieldName + " cannot be null");
        return obj;
    }
}
