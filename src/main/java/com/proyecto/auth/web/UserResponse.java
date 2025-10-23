package com.proyecto.auth.web;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String name,
    String email,
    String role,
    UUID supervisorId,
    UUID teamId
) {}
