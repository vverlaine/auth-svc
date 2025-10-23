package com.proyecto.auth.controller;

import com.proyecto.auth.model.User;
import com.proyecto.auth.repo.UserRepository;
import com.proyecto.auth.service.PasswordService;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.proyecto.auth.web.UserResponse;

@RestController
@RequestMapping("/auth")
@CrossOrigin // permite llamadas desde el portal sin gateway
public class AuthController {

    // ---------- DTOs ----------
    private record LoginRequest(@Email String email, @NotBlank String password) {

    }

    private record LoginResponse(UUID id, String email, String name, String role) {

    }

    public record RegisterRequest(String email, String password, String name, String role) {

    }

    public record ErrorResponse(String error) {

    }

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
        return ResponseEntity.ok(new LoginResponse(user.getId(), user.getEmail(), user.getName(), user.getRole().name()));
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
        return ResponseEntity.ok(new LoginResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        var users = repo.findAll().stream()
                .map(u -> new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name()
        ))
                .toList();
        return ResponseEntity.ok(users);
    }

    // Eliminar usuario por id
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable java.util.UUID id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Cambiar rol de usuario por id
    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable java.util.UUID id, @RequestParam String role) {
        var userOpt = repo.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();
        try {
            user.setRole(User.Role.valueOf(role.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid role"));
        }
        repo.save(user);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name()));
    }
}
