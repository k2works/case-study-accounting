package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class HealthControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    @SuppressWarnings("unchecked")
    void healthEndpointReturnsUpStatus() {
        RestClient restClient = RestClient.create("http://localhost:" + port);

        Map<String, Object> response = restClient.get()
                .uri("/api/health")
                .retrieve()
                .body(Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("UP");

        Map<String, Object> database = (Map<String, Object>) response.get("database");
        assertThat(database.get("status")).isEqualTo("UP");
        assertThat(database.get("product")).isEqualTo("PostgreSQL");
    }
}
