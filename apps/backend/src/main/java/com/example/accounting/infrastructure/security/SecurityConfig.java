package com.example.accounting.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 設定
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    // ロール定数
    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_MANAGER = "MANAGER";

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, JwtAuthenticationEntryPoint jwtAuthEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntryPoint = jwtAuthEntryPoint;
    }

    @Bean
    @SuppressWarnings("java:S4502") // CSRF 無効化は JWT ベースのステートレス認証では安全
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                // CSRF 無効化: JWT ベースのステートレス認証では CSRF 保護は不要
                // - JWT は Authorization ヘッダーで送信（Cookie ではない）
                // - セッションは STATELESS（Cookie ベースのセッションなし）
                // - CSRF 攻撃はブラウザの自動 Cookie 送信を悪用するが、
                //   Authorization ヘッダーは自動送信されないため脅威なし
                // See: https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-when
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 設定
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ステートレスセッション
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 認証エントリーポイント
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthEntryPoint))

                // 認可設定
                .authorizeHttpRequests(auth -> auth
                        // 認証不要
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // Swagger UI / OpenAPI
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs.yaml").permitAll()
                        .requestMatchers("/api-docs/**").permitAll()

                        // ユーザー管理は ADMIN のみ
                        .requestMatchers("/api/users/**").hasRole(ROLE_ADMIN)

                        // 仕訳承認は MANAGER 以上
                        .requestMatchers(HttpMethod.POST, "/api/journals/*/approve")
                        .hasAnyRole(ROLE_ADMIN, ROLE_MANAGER)

                        // 仕訳確定は MANAGER 以上
                        .requestMatchers(HttpMethod.POST, "/api/journals/*/confirm")
                        .hasAnyRole(ROLE_ADMIN, ROLE_MANAGER)

                        // 財務分析は MANAGER 以上
                        .requestMatchers("/api/analysis/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_MANAGER)

                        // その他は認証必須
                        .anyRequest().authenticated()
                )

                // H2 Console 用のフレームオプション許可
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))

                // JWT フィルタを追加
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
