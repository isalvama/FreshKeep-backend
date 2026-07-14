package com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token;

import com.isalvama.fresh_keep.modules.account.application.port.out.TokenRepositoryPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Token;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.token.mapper.TokenMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TokenRepositoryAdapter implements TokenRepositoryPort {
    private final TokenSpringDataRepository tokenSpringDataRepository;
    private final TokenMapper tokenMapper;


    @Override
    public Token save(Token token) {
        JpaTokenEntity entity = tokenSpringDataRepository.save(tokenMapper.toEntity(token));
        return tokenMapper.toDomain(entity);
    }
}
