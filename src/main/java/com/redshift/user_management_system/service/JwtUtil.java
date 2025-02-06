package com.redshift.user_management_system.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // Expiration time in milliseconds
    private long expirationTime;

    private SecretKey secretKey;

    // ✅ Generate a secure 256-bit key at startup
    @PostConstruct
    public void init() {
        this.secretKey = generateSecureKey(secret);
    }

    // ✅ Method to generate a secure 256-bit key from a short secret
    private SecretKey generateSecureKey(String shortSecret) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(shortSecret.getBytes(StandardCharsets.UTF_8));

            log.info("Derived JWT Secret Key (Base64): {}", Base64.getEncoder().encodeToString(keyBytes));

            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secure key", e);
        }
    }

    @Bean
    public SecretKey getSecretKey() {
        return secretKey;
    }

    // ✅ Generate Token
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date()) // Token issued time
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // Expiry
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract Username
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // ✅ Extract Expiration Date
    public Date extractExpiration(String token) {
        return extractClaims(token).getExpiration();
    }

    // ✅ Extract Claims
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Validate Token
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    // ✅ Check if Token Expired
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
