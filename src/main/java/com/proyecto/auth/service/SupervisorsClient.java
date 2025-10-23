package com.proyecto.auth.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SupervisorsClient {

    private static final Logger log = LoggerFactory.getLogger(SupervisorsClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SupervisorsClient(RestTemplate restTemplate,
                             @Value("${ops.supervisors.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public List<SupervisorInfo> fetchAll() {
        try {
            ResponseEntity<SupervisorInfo[]> response =
                    restTemplate.getForEntity(baseUrl + "/supervisors", SupervisorInfo[].class);
            if (response.getBody() == null) {
                return Collections.emptyList();
            }
            return new ArrayList<>(Arrays.asList(response.getBody()));
        } catch (RestClientException ex) {
            log.error("Failed to fetch supervisors list from {}", baseUrl, ex);
            throw new IllegalStateException("No se pudo consultar el servicio de supervisores", ex);
        }
    }

    public Optional<SupervisorInfo> fetchById(UUID supervisorId) {
        try {
            ResponseEntity<SupervisorInfo> response =
                    restTemplate.getForEntity(baseUrl + "/supervisors/{id}", SupervisorInfo.class, supervisorId);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.error("Failed to fetch supervisor {} from {}", supervisorId, baseUrl, ex);
            throw new IllegalStateException("No se pudo validar el supervisor indicado", ex);
        }
    }
}
