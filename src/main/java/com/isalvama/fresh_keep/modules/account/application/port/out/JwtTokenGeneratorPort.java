package com.isalvama.fresh_keep.modules.account.application.port.out;


import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.AuthToken;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;

import java.util.Map;

public interface JwtTokenGeneratorPort {
    AuthToken generateToken(Account account);
    AuthToken generateToken(Account account, Map<String, Object> extraClaims);
    CustomUserPrincipal extractCustomUserPrincipal(String token);
    boolean isTokenValid (String token);
    long getExpirationTime();
}