package com.example.accounting.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger UI 設定
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Accounting API}")
    private String applicationName;

    @Value("${openapi.server.url:}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        List<Server> servers = new java.util.ArrayList<>();

        // 環境変数で指定されたサーバー（Heroku等）
        if (serverUrl != null && !serverUrl.isBlank()) {
            servers.add(new Server()
                    .url(serverUrl)
                    .description("本番/デモ環境"));
        }

        // デフォルトサーバー
        servers.add(new Server()
                .url("http://localhost:8080")
                .description("ローカル開発環境"));
        servers.add(new Server()
                .url("http://localhost:8081")
                .description("Docker Compose 環境"));

        return new OpenAPI()
                .info(new Info()
                        .title("財務会計システム API")
                        .description("""
                                財務会計システムの REST API ドキュメント

                                ## 認証

                                ほとんどの API は JWT 認証が必要です。

                                1. `/api/auth/login` でログインしてアクセストークンを取得
                                2. 取得したトークンを `Authorization: Bearer {token}` ヘッダーに設定

                                ## テストユーザー

                                | ユーザー名 | パスワード | ロール |
                                |-----------|-----------|-------|
                                | admin | Password123! | ADMIN |
                                | manager | Password123! | MANAGER |
                                | user | Password123! | USER |
                                | viewer | Password123! | VIEWER |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("開発チーム")
                                .email("dev@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT アクセストークンを入力してください")));
    }
}
