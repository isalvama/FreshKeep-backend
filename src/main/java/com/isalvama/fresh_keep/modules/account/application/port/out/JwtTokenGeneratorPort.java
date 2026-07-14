package com.isalvama.fresh_keep.modules.account.application.port.out;


import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthToken;

import java.util.Map;

public interface JwtTokenGeneratorPort {
    AuthToken generateToken(Map<String, Object> extraClaims, Account account);
    AuthToken generateToken(Account account);
}