package com.proyecto.auth.service;

import com.proyecto.auth.entity.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;      // <-- ESTE IMPORT
import io.jsonwebtoken.security.Keys;    // <-- Y ESTE
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expirationMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes
    ) {
        // secret debe venir en Base64 (>= 256 bits). Ej: export APP_JWT_SECRET="$(openssl rand -base64 48)"
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMinutes * 60_000;
    }

    public String generate(String subjectEmail, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(subjectEmail)
                .addClaims(Map.of("role", role.name()))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key)
                .compact();
    }
}