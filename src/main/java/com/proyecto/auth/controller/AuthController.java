package com.proyecto.auth.controller;

import com.proyecto.auth.model.User;
import com.proyecto.auth.repo.UserRepository;
import com.proyecto.auth.service.JwtService;
import com.proyecto.auth.service.PasswordService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private record LoginRequest(@Email String email, @NotBlank String password) {

    }

    private record LoginResponse(String token, String role, String name) {

    }

    private final UserRepository repo;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthController(UserRepository repo, PasswordService passwordService, JwtService jwtService) {
        this.repo = repo;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var user = repo.findByEmail(req.email()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
        if (!passwordService.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        var token = jwtService.generate(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, user.getRole().name(), user.getName()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(name = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized"));
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        String email;
        try {
            email = jwtService.getSubject(token);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ErrorResponse("Unauthorized"));
        }

        Optional<User> opt = repo.findByEmail(email);
        return opt.<ResponseEntity<?>>map(u -> ResponseEntity.ok(new MeResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name()
        )))
                .orElseGet(() -> ResponseEntity.status(401).body(new ErrorResponse("Unauthorized")));
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
        return ResponseEntity.ok(new MeResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole().name()
        ));
    }

    public record MeResponse(
            UUID id,
            String name,
            String email,
            String role
            ) {

    }

    public record RegisterRequest(String email, String password, String name, String role) {

    }

    public record ErrorResponse(String error) {

    }
}
