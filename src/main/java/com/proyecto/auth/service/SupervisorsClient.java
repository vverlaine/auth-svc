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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SupervisorsClient {

    private static final Logger log = LoggerFactory.getLogger(SupervisorsClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String authHeaderValue;

    public SupervisorsClient(RestTemplate restTemplate,
                             @Value("${ops.supervisors.base-url:#{null}}") String configuredBaseUrl,
                             @Value("${ops.supervisors.auth-token:#{null}}") String configuredAuthToken) {
        this.restTemplate = restTemplate;
        String envBaseUrl = System.getenv("SUPERVISORS_SVC_URL");
        String resolvedBaseUrl = StringUtils.hasText(configuredBaseUrl) ? configuredBaseUrl : envBaseUrl;
        if (!StringUtils.hasText(resolvedBaseUrl)) {
            resolvedBaseUrl = "http://localhost:8096";
        }
        this.baseUrl = resolvedBaseUrl;

        String envAuthToken = System.getenv("SUPERVISORS_SVC_TOKEN");
        String resolvedAuthToken = StringUtils.hasText(configuredAuthToken) ? configuredAuthToken : envAuthToken;
        if (StringUtils.hasText(resolvedAuthToken)) {
            this.authHeaderValue = resolvedAuthToken.startsWith("Bearer ") || resolvedAuthToken.startsWith("Basic ")
                    ? resolvedAuthToken
                    : "Bearer " + resolvedAuthToken;
        } else {
            this.authHeaderValue = null;
        }
    }

    public List<SupervisorInfo> fetchAll() {
        try {
            ResponseEntity<SupervisorInfo[]> response =
                    restTemplate.exchange(baseUrl + "/supervisors", HttpMethod.GET, httpEntity(), SupervisorInfo[].class);
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
                    restTemplate.exchange(baseUrl + "/supervisors/{id}", HttpMethod.GET, httpEntity(), SupervisorInfo.class, supervisorId);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound ex) {
            return Optional.empty();
        } catch (RestClientException ex) {
            log.error("Failed to fetch supervisor {} from {}", supervisorId, baseUrl, ex);
            throw new IllegalStateException("No se pudo validar el supervisor indicado", ex);
        }
    }

    private HttpEntity<?> httpEntity() {
        if (authHeaderValue == null) {
            return HttpEntity.EMPTY;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authHeaderValue);
        return new HttpEntity<>(headers);
    }
}
