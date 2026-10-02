package com.prodman.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
public class JwtService {

    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_TENANT_ID = "tenantId";
    public static final String CLAIM_TENANT_STATUS = "tenantStatus";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractTenantId(String token) {
        String raw = extractClaim(token, c -> c.get(CLAIM_TENANT_ID, String.class));
        return raw == null ? null : UUID.fromString(raw);
    }

    public UUID extractUserId(String token) {
        String raw = extractClaim(token, c -> c.get(CLAIM_USER_ID, String.class));
        return raw == null ? null : UUID.fromString(raw);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Генерация access-токена.
     * @param username имя пользователя (subject)
     * @param userId UUID пользователя (может быть null для старых тестовых)
     * @param role роль (например, "ROLE_ADMIN")
     * @param tenantId UUID тенанта (null для PLATFORM_ADMIN)
     * @param tenantStatus статус тенанта (null для PLATFORM_ADMIN)
     */
    public String generateToken(String username,
                                UUID userId,
                                String role,
                                UUID tenantId,
                                String tenantStatus) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) claims.put(CLAIM_USER_ID, userId.toString());
        if (role != null) claims.put(CLAIM_ROLE, role);
        if (tenantId != null) claims.put(CLAIM_TENANT_ID, tenantId.toString());
        if (tenantStatus != null) claims.put(CLAIM_TENANT_STATUS, tenantStatus);
        return buildToken(claims, username, jwtExpiration);
    }

    public String generateRefreshToken(String username,
                                       UUID userId,
                                       UUID tenantId) {
        Map<String, Object> claims = new HashMap<>();
        if (userId != null) claims.put(CLAIM_USER_ID, userId.toString());
        if (tenantId != null) claims.put(CLAIM_TENANT_ID, tenantId.toString());
        return buildToken(claims, username, refreshExpiration);
    }

    private String buildToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}