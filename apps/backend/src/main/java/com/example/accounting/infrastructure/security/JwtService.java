package com.example.accounting.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * JWT トークンの生成と検証を行うサービス
 *
 * <p>Clock を DI することで、テスト時に固定時刻を注入可能。</p>
 */
@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;
    private final Clock clock;

    /**
     * コンストラクタ
     *
     * @param jwtProperties JWT 設定
     * @param clock         時刻取得用クロック（テスト時に固定クロックを注入可能）
     */
    public JwtService(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.clock = clock;
    }

    public String generateToken(String subject) {
        return generateToken(subject, Map.of());
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        return buildToken(subject, claims, jwtProperties.expiration());
    }

    public String generateRefreshToken(String subject) {
        return buildToken(subject, Map.of(), jwtProperties.refreshExpiration());
    }

    private String buildToken(String subject, Map<String, Object> claims, long expiration) {
        Instant now = clock.instant();
        Instant expiryInstant = now.plusMillis(expiration);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryInstant))
                .signWith(secretKey)
                .compact();
    }

    public Optional<String> extractSubject(String token) {
        return extractClaims(token).map(Claims::getSubject);
    }

    public Optional<Claims> extractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException _) {
            return Optional.empty();
        }
    }

    public boolean isTokenValid(String token) {
        return extractClaims(token)
                .map(claims -> !isTokenExpired(claims))
                .orElse(false);
    }

    public boolean isTokenValid(String token, String expectedSubject) {
        return extractClaims(token)
                .map(claims -> claims.getSubject().equals(expectedSubject) && !isTokenExpired(claims))
                .orElse(false);
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(Date.from(clock.instant()));
    }
}
