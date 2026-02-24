package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.in.RecordAuditLogUseCase;
import com.example.accounting.application.port.in.RecordAuditLogUseCase.RecordAuditLogCommand;
import com.example.accounting.application.port.out.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.infrastructure.web.dto.LoginRequest;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 認証コントローラ
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "認証", description = "ユーザー認証に関する API")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final AuthUseCase authUseCase;
    private final RecordAuditLogUseCase recordAuditLogUseCase;

    public AuthController(AuthUseCase authUseCase, RecordAuditLogUseCase recordAuditLogUseCase) {
        this.authUseCase = authUseCase;
        this.recordAuditLogUseCase = recordAuditLogUseCase;
    }

    /**
     * ログイン
     */
    @Operation(
            summary = "ログイン",
            description = "ユーザー名とパスワードで認証し、JWT トークンを取得します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "ログイン成功",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "バリデーションエラー",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "認証失敗（ユーザー名またはパスワードが不正）",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpServletRequest) {
        LoginCommand command = new LoginCommand(request.username(), request.password());
        LoginResult result = authUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    new RecordAuditLogCommand(
                            request.username(),
                            AuditAction.LOGIN,
                            null,
                            null,
                            "ログイン成功",
                            httpServletRequest.getRemoteAddr()
                    )
            );
            return ResponseEntity.ok(LoginResponse.success(
                    result.accessToken(),
                    result.refreshToken(),
                    result.username(),
                    result.role().name()
            ));
        } else {
            recordAuditLogSafely(
                    new RecordAuditLogCommand(
                            request.username(),
                            AuditAction.LOGIN,
                            null,
                            null,
                            "ログイン失敗",
                            httpServletRequest.getRemoteAddr()
                    )
            );
            return ResponseEntity.status(401).body(LoginResponse.failure(result.errorMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Principal principal, HttpServletRequest httpServletRequest) {
        if (principal != null) {
            recordAuditLogSafely(
                    new RecordAuditLogCommand(
                            principal.getName(),
                            AuditAction.LOGOUT,
                            null,
                            null,
                            "ログアウト",
                            httpServletRequest.getRemoteAddr()
                    )
            );
        }
        return ResponseEntity.ok().build();
    }

    private void recordAuditLogSafely(RecordAuditLogCommand command) {
        try {
            recordAuditLogUseCase.execute(command);
        } catch (RuntimeException ex) {
            LOGGER.warn("監査ログ記録に失敗しました。 userId={}", command.userId(), ex);
        }
    }
}
