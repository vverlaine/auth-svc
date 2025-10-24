package com.proyecto.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private final Key secretKey;

    public JwtService(@Value("${jwt.secret:${APP_JWT_SECRET:}}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret not configured. Set APP_JWT_SECRET or jwt.secret");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generate(String subject, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000L * 60 * 60 * 8))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
