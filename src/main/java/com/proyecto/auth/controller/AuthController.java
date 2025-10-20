package com.proyecto.auth.controller;

import com.proyecto.auth.model.User;
import com.proyecto.auth.repo.UserRepository;
import com.proyecto.auth.service.PasswordService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin // permite llamadas desde el portal sin gateway
public class AuthController {

    // ---------- DTOs ----------
    private record LoginRequest(@Email String email, @NotBlank String password) { }
    private record LoginResponse(String email, String name, String role) { }
    public  record RegisterRequest(String email, String password, String name, String role) { }
    public  record ErrorResponse(String error) { }

    // ---------- Dependencias ----------
    private final UserRepository repo;
    private final PasswordService passwordService;

    public AuthController(UserRepository repo, PasswordService passwordService) {
        this.repo = repo;
        this.passwordService = passwordService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var user = repo.findByEmail(req.email()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid credentials"));
        }
        if (!passwordService.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(new ErrorResponse("Invalid credentials"));
        }
        // Sin token: regresamos datos necesarios para el portal (email/name/role)
        return ResponseEntity.ok(new LoginResponse(user.getEmail(), user.getName(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req.email() == null || req.password() == null || req.name() == null || req.role() == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Missing fields"));
        }
        if (repo.existsByEmail(req.email())) {
            return ResponseEntity.status(409).body(new ErrorResponse("Email already registered"));
        }

        var u = new User();
        u.setEmail(req.email().trim().toLowerCase());
        u.setName(req.name().trim());

        User.Role role;
        try {
            role = User.Role.valueOf(req.role().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid role"));
        }
        u.setRole(role);
        u.setPasswordHash(passwordService.encode(req.password()));

        var saved = repo.save(u);
        return ResponseEntity.ok(new LoginResponse(saved.getEmail(), saved.getName(), saved.getRole().name()));
    }
}