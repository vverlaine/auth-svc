package com.proyecto.auth.config;

import com.proyecto.auth.model.User;
import com.proyecto.auth.repo.UserRepository;
import com.proyecto.auth.service.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner load(UserRepository repo, PasswordService ps) {
        return args -> {
            if (!repo.existsByEmail("admin@demo.com")) {
                User u = new User();
                u.setEmail("admin@demo.com");
                u.setName("Admin Demo");
                u.setRole(User.Role.ADMIN);         // enum interno en User
                u.setPasswordHash(ps.encode("admin123"));
                repo.save(u);
            }
        };
    }
}