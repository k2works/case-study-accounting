package com.example.accounting.presentation.api.auth;

import com.example.accounting.application.usecase.auth.LoginCommand;
import com.example.accounting.application.usecase.auth.LoginResult;
import com.example.accounting.application.usecase.auth.LoginUseCase;
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
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    /**
     * ログイン
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginCommand command = new LoginCommand(request.username(), request.password());
        LoginResult result = loginUseCase.execute(command);

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
