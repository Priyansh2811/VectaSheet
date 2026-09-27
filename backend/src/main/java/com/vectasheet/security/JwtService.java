package com.vectasheet.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {

    private final SecretKey key;
    private final long accessTokenTtlMs;

    public JwtService(
            @Value("${vectasheet.jwt.secret}") String secret,
            @Value("${vectasheet.jwt.access-token-ttl-minutes:15}") long accessTokenTtlMinutes
    ) {
        // Pad/derive a 256-bit key from the configured secret.
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(padTo32Bytes(raw));
        this.accessTokenTtlMs = accessTokenTtlMinutes * 60_000;
    }

    private byte[] padTo32Bytes(byte[] input) {
        if (input.length >= 32) return input;
        byte[] padded = new byte[32];
        System.arraycopy(input, 0, padded, 0, input.length);
        return padded;
    }

    public String generateAccessToken(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenTtlMs)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
