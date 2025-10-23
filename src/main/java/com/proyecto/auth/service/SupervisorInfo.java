package com.proyecto.auth.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SupervisorInfo(UUID id, String name, String email) {
}
