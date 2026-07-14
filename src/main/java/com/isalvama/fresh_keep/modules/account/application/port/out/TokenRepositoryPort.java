package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.domain.model.Token;

public interface TokenRepositoryPort {
    public Token save(Token token);
}
