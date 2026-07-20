package com.isalvama.fresh_keep.modules.account.infrastructure.security.token;

import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;


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

        List<String> roleNames = customUserPrincipal.roles().stream()
                .map(r -> "ROLE_" + r)
                .toList();

        String token = Jwts.builder()
                .claims(extraClaims)
                .claim("roles", roleNames)
                .claim("userId", customUserPrincipal.id())
                .subject(customUserPrincipal.getUsername())
                .issuedAt(currentDate)
                .expiration(expirationDate)
                .signWith(getSignInKey())
                .compact();
        return new AuthToken(token);
    }

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
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream().map(Objects::toString).map(r ->
            r.startsWith("ROLE_") ? r.substring(5) : r).toList();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public boolean isTokenValid (String token){
            try {
                extractAllClaims(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
