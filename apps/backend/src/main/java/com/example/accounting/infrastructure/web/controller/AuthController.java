package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.AuthUseCase;
import com.example.accounting.application.port.out.LoginResult;
import com.example.accounting.application.port.in.command.LoginCommand;
import com.example.accounting.infrastructure.web.dto.LoginRequest;
import com.example.accounting.infrastructure.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
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
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.username(), request.password());
        LoginResult result = authUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(LoginResponse.success(
                    result.accessToken(),
                    result.refreshToken(),
                    result.username(),
                    result.role().name()
            ));
        } else {
            return ResponseEntity.status(401).body(LoginResponse.failure(result.errorMessage()));
        }
    }
}
