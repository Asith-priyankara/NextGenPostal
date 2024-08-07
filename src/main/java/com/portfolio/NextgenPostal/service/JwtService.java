package com.portfolio.NextgenPostal.service;

import com.portfolio.NextgenPostal.Entity.TokenEntity;
import com.portfolio.NextgenPostal.Entity.UserEntity;
import com.portfolio.NextgenPostal.Repository.TokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey ;

    @Value("${application.security.jwt.expiration}")
    private long  jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration:}")
    private long refreshExpiration;

    private final TokenRepository tokenRepository;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map extraClaims = new HashMap<String,Objects>();
        extraClaims.put("refreshToken", true);
        return buildToken(extraClaims, userDetails, refreshExpiration);
    }
    private String buildToken(
            Map<String, Objects> extraClaims,
            UserDetails userDetails,
            long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaims(token, Claims::getExpiration);
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    public boolean isTokenRevokedOrExpired(String token) {
        var storedToken = tokenRepository.findByToken(token).orElse(null);
        if (storedToken != null) {
            return storedToken.isExpired() || storedToken.isRevoked();
        }
        return true;
    }

    public boolean isValidRefreshToken(String refreshToken, UserEntity user) {
        return (isTokenValid(refreshToken,user) && isRefreshToken(refreshToken));
    }

    private boolean isRefreshToken(String refreshToken) {
        return extractClaims(refreshToken, claims -> claims.get("refreshToken", Boolean.class));
    }
}
