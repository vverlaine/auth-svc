package com.proyecto.auth.controller;

import com.proyecto.auth.model.TeamMember;
import com.proyecto.auth.model.User;
import com.proyecto.auth.repo.TeamMemberRepository;
import com.proyecto.auth.repo.UserRepository;
import com.proyecto.auth.service.PasswordService;
import com.proyecto.auth.service.SupervisorInfo;
import com.proyecto.auth.service.SupervisorsClient;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.proyecto.auth.web.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
@CrossOrigin // permite llamadas desde el portal sin gateway
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    // ---------- DTOs ----------
    private record LoginRequest(@Email String email, @NotBlank String password) {

    }

    private record LoginResponse(UUID id, String email, String name, String role) {

    }

    public record RegisterRequest(String email, String password, String name, String role, UUID supervisorId) {

    }

    public record ErrorResponse(String error) {

    }

    // ---------- Dependencias ----------
    private final UserRepository repo;
    private final PasswordService passwordService;
    private final TeamMemberRepository teamMemberRepository;
    private final SupervisorsClient supervisorsClient;

    public AuthController(UserRepository repo,
                          PasswordService passwordService,
                          TeamMemberRepository teamMemberRepository,
                          SupervisorsClient supervisorsClient) {
        this.repo = repo;
        this.passwordService = passwordService;
        this.teamMemberRepository = teamMemberRepository;
        this.supervisorsClient = supervisorsClient;
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
    @Transactional
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

        UUID supervisorId = null;
        if (role == User.Role.TECNICO) {
            supervisorId = req.supervisorId();
            if (supervisorId == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Supervisor required for TECNICO role"));
            }
            try {
                var supervisorOpt = supervisorsClient.fetchById(supervisorId);
                if (supervisorOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(new ErrorResponse("Supervisor not found"));
                }
            } catch (IllegalStateException ex) {
                log.error("Error calling supervisors service", ex);
                return ResponseEntity.status(503).body(new ErrorResponse("No se pudo validar el supervisor indicado"));
            }
        }
        u.setRole(role);
        u.setPasswordHash(passwordService.encode(req.password()));

        var saved = repo.save(u);

        if (role == User.Role.TECNICO) {
            teamMemberRepository.save(new TeamMember(supervisorId, saved.getId()));
        }

        return ResponseEntity.ok(new LoginResponse(saved.getId(), saved.getEmail(), saved.getName(), saved.getRole().name()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> listUsers() {
        Map<UUID, UUID> supervisorByUser = teamMemberRepository.findAll().stream()
                .collect(Collectors.toMap(
                        TeamMember::getUserId,
                        TeamMember::getTeamId,
                        (existing, replacement) -> existing
                ));
        var users = repo.findAll().stream()
                .map(u -> {
                    UUID supervisorId = supervisorByUser.get(u.getId());
                    return new UserResponse(
                            u.getId(),
                            u.getName(),
                            u.getEmail(),
                            u.getRole().name(),
                            supervisorId,
                            supervisorId
                    );
                })
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
        UUID supervisorId = teamMemberRepository.findFirstByIdUserId(user.getId())
                .map(TeamMember::getTeamId)
                .orElse(null);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                supervisorId,
                supervisorId
        ));
    }

    @GetMapping("/supervisors")
    public ResponseEntity<?> listSupervisors() {
        try {
            List<SupervisorInfo> supervisors = supervisorsClient.fetchAll();
            return ResponseEntity.ok(supervisors);
        } catch (IllegalStateException ex) {
            log.error("Error fetching supervisors list", ex);
            return ResponseEntity.status(503).body(new ErrorResponse("No se pudieron obtener los supervisores"));
        }
    }
}
