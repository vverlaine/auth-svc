package com.proyecto.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public String hash(String raw) { return encoder.encode(raw); }
    public boolean matches(String raw, String hash) { return encoder.matches(raw, hash); }
}