package com.isalvama.fresh_keep.modules.account.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountException;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.shared.domain.Role;
import lombok.Getter;

import java.util.Set;

@Getter
public class Account {
    private final AccountId id;
    private final Email email;
    private final String passwordHash;
    private final Set<Role> roles;

    private Account(AccountId id, Email email, String passwordHash, Set<Role> roles){
        if (roles.isEmpty()){
            throw new InvalidAccountException("the set of roles cannot be empty");
        }
        this.id = validateNotNull(id, "id");
        this.email = validateNotNull(email, "email");
        this.passwordHash = validateNotNull(passwordHash, "passwordHash");
        this.roles = validateNotNull(roles, "roles");
    }

    public static Account createUser(Email email, String passwordHash){
        return new Account(
                AccountId.create(),
                email,
                passwordHash,
                Set.of(Role.USER)
        );
    }

    public static Account createAdmin (Email email, String passwordHash){
        return new Account(
                AccountId.create(),
                email,
                passwordHash,
                Set.of(Role.ADMIN)
        );
    }

    public static Account reconstitute (AccountId id, Email email, String passwordHash, Set<Role> roles){
        return new Account(
                id,
                email,
                passwordHash,
                roles
        );
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    private static <T> T validateNotNull(T obj, String fieldName) {
        if (obj == null)
            throw new InvalidAccountException(fieldName + " cannot be null");
        return obj;
    }
}
