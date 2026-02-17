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
import java.util.Map;

/**
 * ヘルスチェックコントローラ
 */
@RestController
@RequestMapping("/api")
@Tag(name = "ヘルスチェック", description = "システムの稼働状態を確認する API")
public class HealthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);
    private static final String STATUS_UP = "UP";
    private static final String STATUS_DOWN = "DOWN";
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    record HealthResponse(String status, String timestamp, Map<String, Object> database) {}

    @Operation(
            summary = "ヘルスチェック",
            description = "アプリケーションとデータベースの稼働状態を返します"
    )
    @ApiResponse(responseCode = "200", description = "システム稼働状態")
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        Map<String, Object> databaseStatus = checkDatabase();

        var response = new HealthResponse(STATUS_UP, Instant.now().toString(), databaseStatus);

        return ResponseEntity.ok(response);
    }

    private Map<String, Object> checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            return Map.of(
                    "status", STATUS_UP,
                    "product", connection.getMetaData().getDatabaseProductName(),
                    "version", connection.getMetaData().getDatabaseProductVersion()
            );
        } catch (SQLException e) {
            LOGGER.warn("Database health check failed", e);
            return Map.of(
                    "status", STATUS_DOWN,
                    "error", e.getMessage()
            );
        }
    }
}
