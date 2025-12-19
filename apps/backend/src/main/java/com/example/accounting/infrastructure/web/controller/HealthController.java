package com.example.accounting.infrastructure.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        try (Connection connection = dataSource.getConnection()) {
            response.put("database", Map.of(
                    "status", "UP",
                    "product", connection.getMetaData().getDatabaseProductName(),
                    "version", connection.getMetaData().getDatabaseProductVersion()
            ));
        } catch (Exception e) {
            response.put("database", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
        }

        return ResponseEntity.ok(response);
    }
}
