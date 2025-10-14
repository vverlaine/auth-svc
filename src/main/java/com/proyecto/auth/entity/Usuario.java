package com.proyecto.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuario")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Usuario {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable=false)
  private String email;

  @Column(nullable=false)
  private String passwordHash;

  @Column(nullable=false)
  private String nombre;

  @Enumerated(EnumType.STRING)
  @Column(nullable=false)
  private Role rol;
}