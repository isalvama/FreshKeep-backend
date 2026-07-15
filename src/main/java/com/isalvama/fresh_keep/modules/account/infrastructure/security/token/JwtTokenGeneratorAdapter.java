package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class JwtTokenGeneratorAdapter implements JwtTokenGeneratorPort {
    private final CustomUserPrincipalMapper customUserPrincipalMapper;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public AuthToken generateToken(Account account){
        return buildToken(customUserPrincipalMapper.fromDomain(account), new HashMap<>());
    }

    @Override
    public AuthToken generateToken(Account account, Map<String, Object> extraClaims){
        return buildToken(customUserPrincipalMapper.fromDomain(account), extraClaims);
    }
    public AuthToken buildToken(
            CustomUserPrincipal customUserPrincipal,
            Map<String, Object> extraClaims
            ) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);

        String token = Jwts.builder()
                .claims(extraClaims)
                .claim("role", customUserPrincipal.role())
                .claim("userId", customUserPrincipal.id())
                .subject(customUserPrincipal.getUsername())
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
        return new AuthToken(token);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
