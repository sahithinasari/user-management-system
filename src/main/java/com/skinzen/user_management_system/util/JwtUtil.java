package com.skinzen.user_management_system.util;

import com.skinzen.user_management_system.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // seconds
    private long expirationTime;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = deriveKey(secret);
    }

    // Derive a strong 256-bit key ONCE
    private SecretKey deriveKey(String rawSecret) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(rawSecret.getBytes(StandardCharsets.UTF_8));

            log.info("JWT signing key initialized (256-bit)");

            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to initialize JWT key", e);
        }
    }

    // ================= TOKEN GENERATION =================

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + expirationTime * 1000)
                )
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public long getAccessTokenExpiry() {
        return expirationTime;
    }

    // ================= TOKEN VALIDATION =================

    public boolean validateToken(String token, String userId) {
        return extractSubject(token).equals(userId) && !isTokenExpired(token);
    }

    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
