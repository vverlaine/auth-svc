package com.proyecto.auth.config;

import com.proyecto.auth.entity.Role;
import com.proyecto.auth.entity.Usuario;
import com.proyecto.auth.repository.UsuarioRepository;
import com.proyecto.auth.service.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
  @Bean
  CommandLineRunner load(UsuarioRepository repo, PasswordService ps) {
    return args -> {
      if (repo.count() == 0) {
        Usuario u1 = new Usuario();
        u1.setEmail("admin@demo.com");
        u1.setPasswordHash(ps.hash("admin123"));
        u1.setNombre("Admin Demo");
        u1.setRol(Role.ADMIN);
        repo.save(u1);

        Usuario u2 = new Usuario();
        u2.setEmail("supervisor@demo.com");
        u2.setPasswordHash(ps.hash("sup123"));
        u2.setNombre("Supervisor Demo");
        u2.setRol(Role.SUPERVISOR);
        repo.save(u2);

        Usuario u3 = new Usuario();
        u3.setEmail("tecnico@demo.com");
        u3.setPasswordHash(ps.hash("tec123"));
        u3.setNombre("Técnico Demo");
        u3.setRol(Role.TECNICO);
        repo.save(u3);
      }
    };
  }
}