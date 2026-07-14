package com.isalvama.fresh_keep.modules.account.infrastructure.security;

import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenGeneratorAdapter implements JwtTokenGeneratorPort {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }

    @Override
    public AuthToken generateToken(Map<String, Object> extraClaims, Account account){
        return buildToken(extraClaims, CustomUserPrincipal.fromDomain(account));
    }

    @Override
    public AuthToken generateToken(Account account){
        return buildToken(new HashMap<>(), CustomUserPrincipal.fromDomain(account));
    }
    public AuthToken buildToken(
            Map<String, Object> extraClaims,
            CustomUserPrincipal customUserPrincipal
    ) {
        Date currentDate = new Date(System.currentTimeMillis());
        Date expirationDate = new Date(System.currentTimeMillis() + jwtExpiration);

        String token = Jwts.builder()
                .claims(extraClaims)
                .claim("userId", customUserPrincipal.getId())
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

//    public boolean isTokenValid(String token, UserDetails userDetails) {
//        final String username = extractUsername(token);
//        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
//    }

//    private boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }

//    private Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }

//    private Claims extractAllClaims(String token) {
//        return Jwts
//                .parserBuilder()
//                .setSigningKey(getSignInKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
