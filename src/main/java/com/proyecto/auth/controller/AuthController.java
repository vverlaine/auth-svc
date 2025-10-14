package com.proyecto.auth.controller;

import com.proyecto.auth.repository.UsuarioRepository;
import com.proyecto.auth.service.JwtService;
import com.proyecto.auth.service.PasswordService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    private record LoginRequest(@Email String email, @NotBlank String password){}
    private record LoginResponse(String token, String role, String name){}

    private final UsuarioRepository repo;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository repo, PasswordService passwordService, JwtService jwtService) {
        this.repo = repo;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req){
        var user = repo.findByEmail(req.email()).orElse(null);
        if (user == null) return ResponseEntity.status(401).body(Map.of("error","Invalid credentials"));
        if (!passwordService.matches(req.password(), user.getPasswordHash()))
            return ResponseEntity.status(401).body(Map.of("error","Invalid credentials"));

        var token = jwtService.generate(user.getEmail(), user.getRol());
        return ResponseEntity.ok(new LoginResponse(token, user.getRol().name(), user.getNombre()));
    }
}