package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token.mapper;

import com.isalvama.fresh_keep.modules.account.domain.model.Token;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.account.JpaAccountEntity;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token.JpaTokenEntity;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthToken;
import org.springframework.stereotype.Component;


@Component
public class TokenMapper {
    public JpaTokenEntity toEntity(Token token){
        JpaAccountEntity accountProxy = new JpaAccountEntity();
        accountProxy.setId(token.getUserId().value());
        return new JpaTokenEntity(
                token.getId() != null ? token.getId() : null,
                token.getAuthToken().token(),
                JpaTokenEntity.TokenType.BEARER,
                token.isRevoked(),
                token.isExpired(),
                accountProxy
        );
    }

    public Token toDomain(JpaTokenEntity entity){
        return Token.reconstitute(
                entity.getId(),
                AuthToken.from(entity.getToken()),
                AccountId.of(entity.getAccount().getId()),
                entity.isRevoked(),
                entity.isExpired()
        );
    }
}
