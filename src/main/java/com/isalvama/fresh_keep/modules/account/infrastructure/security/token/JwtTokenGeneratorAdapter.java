package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;


@Service
public class JwtTokenGeneratorAdapter implements JwtTokenGeneratorPort {
    private final CustomUserPrincipalMapper customUserPrincipalMapper;
    private final String secretKey;
    private final long jwtExpiration;


    public JwtTokenGeneratorAdapter (
            CustomUserPrincipalMapper customUserPrincipalMapper,
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long jwtExpiration
    ){
      this.customUserPrincipalMapper = customUserPrincipalMapper;
      this.secretKey = secretKey;
      this.jwtExpiration = jwtExpiration;
    }

    @Override
    public AuthToken generateToken(Account account){
        return buildToken(customUserPrincipalMapper.fromDomain(account), new HashMap<>());
    }

    @Override
    public AuthToken generateToken(Account account, Map<String, Object> extraClaims){
        return buildToken(customUserPrincipalMapper.fromDomain(account), extraClaims);
    }
    private AuthToken buildToken(
            CustomUserPrincipal customUserPrincipal,
            Map<String, Object> extraClaims
            ) {

        long now = System.currentTimeMillis();

        List<String> roleNames = customUserPrincipal.roles().stream()
                .map(r -> "ROLE_" + r)
                .toList();

        String token = Jwts.builder()
                .claims(extraClaims)
                .claim("roles", roleNames)
                .claim("userId", customUserPrincipal.id())
                .subject(customUserPrincipal.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpiration))
                .signWith(getSignInKey())
                .compact();

        return new AuthToken(token, jwtExpiration);
    }

    @Override
    public CustomUserPrincipal extractCustomUserPrincipal(String token){
        Claims claims = extractAllClaims(token);
        return new CustomUserPrincipal(
                claims.get("userId", String.class),
                claims.getSubject(),
                null,
                extractRoles(claims.get("roles", List.class))
        );
    }

    private List<String> extractRoles (List<String> roles){
        if (roles == null) {
            return List.of();
        }
        return roles.stream().map(Objects::toString).map(r ->
            r.startsWith("ROLE_") ? r.substring(5) : r).toList();
    }

    @Override
    public long getExpirationTime() {
        return jwtExpiration;
    }

    @Override
    public boolean isTokenValid (String token){
            try {
                Claims claims = extractAllClaims(token);
                String email = claims.getSubject();
                return !(email == null) && !(email.isBlank());
            } catch (Exception e) {
                return false;
            }
        }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
