package com.isalvama.fresh_keep.modules.admin.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.AdminId;
import com.isalvama.fresh_keep.modules.user.domain.exception.InvalidUserException;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import lombok.Getter;

@Getter
public class Admin {
    private final AdminId id;
    private final AccountId accountId;
    private Email email;

    private Admin(AdminId id, AccountId accountId, Email email) {
        this.id = validateNotNull(id, "id");
        this.accountId = validateNotNull(accountId, "accountId");
        this.email = validateNotNull(email, "email");
    }

    public static Admin create (AccountId accountId, Email email){
        return new Admin(
                AdminId.create(),
                accountId,
                email
        );
    }

    public static Admin reconstitute (AdminId id, AccountId accountId, Email email){
        return new Admin(
                id,
                accountId,
                email
        );
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidUserException(fieldName + " cannot be null.");
        return fieldValue;
    }
}
