package com.isalvama.fresh_keep.modules.user.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserName;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.user.domain.exception.InvalidUserException;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private final UserId id;
    private final AccountId accountId;
    private Email email;
    private UserName userName;

    private User(UserId id, AccountId accountId, Email email) {
        this.id = validateNotNull(id, "id");
        this.accountId = validateNotNull(accountId, "accountId");
        this.email = validateNotNull(email, "email");
    }

    public static User create (AccountId accountId, Email email){
        return new User(
                UserId.create(),
                accountId,
                email
        );
    }

    public static User reconstitute (UserId id, AccountId accountId, Email email, UserName userName){
        User user = new User(
                id,
                accountId,
                email
        );
        user.setUserName(userName);
        return user;
    }

    private static <T> T validateNotNull(T fieldValue, String fieldName) {
        if (fieldValue == null)
            throw new InvalidUserException(fieldName + " cannot be null.");
        return fieldValue;
    }

}
