package com.example.accounting.infrastructure.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ヘルスチェックコントローラ
 */
@RestController
@RequestMapping("/api")
@Tag(name = "ヘルスチェック", description = "システムの稼働状態を確認する API")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Operation(
            summary = "ヘルスチェック",
            description = "アプリケーションとデータベースの稼働状態を返します"
    )
    @ApiResponse(responseCode = "200", description = "システム稼働状態")
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
        } catch (SQLException e) {
            LOGGER.warn("Database health check failed", e);
            response.put("database", Map.of(
                    "status", "DOWN",
                    "error", e.getMessage()
            ));
        }

        return ResponseEntity.ok(response);
    }
}
