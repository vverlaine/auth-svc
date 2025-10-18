package com.proyecto.auth.web;

import com.proyecto.auth.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UsersController {

    public record UserResponse(UUID id, String name, String email, String role) {}

    private final UserRepository repo;

    public UsersController(UserRepository repo) { this.repo = repo; }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable UUID id) {
        return repo.findById(id)
            .map(u -> ResponseEntity.ok(new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getRole().name())))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/exists")
    public ResponseEntity<Void> exists(@PathVariable UUID id) {
        return repo.existsById(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}