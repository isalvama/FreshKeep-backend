package com.isalvama.fresh_keep.modules.account.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountException;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthToken;
import lombok.Getter;


@Getter
public class Token {
    private Long id;
    private final AuthToken authToken;
    private boolean revoked;
    private boolean expired;
    private final AccountId userId;

    public Token(AuthToken token, AccountId userId) {
        this.authToken = validateNotNull(token, "token");
        this.userId = validateNotNull(userId, "userId");
    }

    public static Token create(AuthToken authToken, AccountId userId){
       Token token = new Token(
                authToken,
                userId
        );
       token.id = null;
       token.revoked = false;
       token.expired = false;
       return token;
    }

    public static Token reconstitute(Long id, AuthToken authToken, AccountId userId, Boolean revoked, Boolean expired){
        Token token = new Token(
                authToken,
                userId
        );
        token.id = validateNotNull(id, "id");
        token.revoked = validateNotNull(revoked, "revoked");
        token.expired = validateNotNull(expired, "expired");
        return token;
    }



    private static <T> T validateNotNull(T obj, String fieldName) {
        if (obj == null)
            throw new InvalidAccountException(fieldName + " cannot be null");
        return obj;
    }
}
