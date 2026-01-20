package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.RegisterUserUseCase;
import com.example.accounting.application.port.in.command.RegisterUserCommand;
import com.example.accounting.application.port.out.RegisterUserResult;
import com.example.accounting.infrastructure.web.dto.RegisterUserRequest;
import com.example.accounting.infrastructure.web.dto.RegisterUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザー登録コントローラ
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "ユーザー登録", description = "ユーザー登録に関する API")
public class RegisterUserController {

    private final RegisterUserUseCase registerUserUseCase;

    public RegisterUserController(RegisterUserUseCase registerUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
    }

    /**
     * ユーザー登録
     */
    @Operation(
            summary = "ユーザー登録",
            description = "管理者がユーザーを登録します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "登録成功",
            content = @Content(schema = @Schema(implementation = RegisterUserResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "登録失敗",
            content = @Content(schema = @Schema(implementation = RegisterUserResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
                request.username(),
                request.email(),
                request.password(),
                request.displayName(),
                request.role()
        );
        RegisterUserResult result = registerUserUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(RegisterUserResponse.success(
                    result.username(),
                    result.email(),
                    result.displayName(),
                    result.role()
            ));
        }
        return ResponseEntity.badRequest().body(RegisterUserResponse.failure(result.errorMessage()));
    }
}
