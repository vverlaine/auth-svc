package com.proyecto.auth.web;

public record RegisterRequest(
        String email,
        String password,
        String name,
        String role
) {}