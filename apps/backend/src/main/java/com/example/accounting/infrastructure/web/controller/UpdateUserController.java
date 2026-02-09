package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.UpdateUserUseCase;
import com.example.accounting.application.port.in.command.UpdateUserCommand;
import com.example.accounting.application.port.out.UpdateUserResult;
import com.example.accounting.infrastructure.web.dto.UpdateUserRequest;
import com.example.accounting.infrastructure.web.dto.UpdateUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザー更新コントローラ
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "ユーザー更新", description = "ユーザー更新に関する API")
@PreAuthorize("hasRole('ADMIN')")
public class UpdateUserController {

    private final UpdateUserUseCase updateUserUseCase;

    public UpdateUserController(UpdateUserUseCase updateUserUseCase) {
        this.updateUserUseCase = updateUserUseCase;
    }

    /**
     * ユーザー更新
     */
    @Operation(
            summary = "ユーザー更新",
            description = "管理者がユーザー情報を更新します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(schema = @Schema(implementation = UpdateUserResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "更新失敗",
            content = @Content(schema = @Schema(implementation = UpdateUserResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @PutMapping("/{id}")
    public ResponseEntity<UpdateUserResponse> update(@PathVariable("id") String id,
                                                     @Valid @RequestBody UpdateUserRequest request) {
        UpdateUserCommand command = new UpdateUserCommand(
                id,
                request.displayName(),
                request.password(),
                request.role()
        );
        UpdateUserResult result = updateUserUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(UpdateUserResponse.success(
                    result.id(),
                    result.username(),
                    result.email(),
                    result.displayName(),
                    result.role()
            ));
        }
        return ResponseEntity.badRequest().body(UpdateUserResponse.failure(result.errorMessage()));
    }
}
