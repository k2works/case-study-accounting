package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.DeleteUserUseCase;
import com.example.accounting.application.port.in.command.DeleteUserCommand;
import com.example.accounting.application.port.out.DeleteUserResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザー削除コントローラ
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "ユーザー削除", description = "ユーザー削除に関する API")
@PreAuthorize("hasRole('ADMIN')")
public class DeleteUserController {

    private final DeleteUserUseCase deleteUserUseCase;

    public DeleteUserController(DeleteUserUseCase deleteUserUseCase) {
        this.deleteUserUseCase = deleteUserUseCase;
    }

    /**
     * ユーザー削除
     */
    @Operation(
            summary = "ユーザー削除",
            description = "管理者がユーザーを削除します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "削除成功",
            content = @Content(schema = @Schema(implementation = DeleteUserResult.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "削除失敗",
            content = @Content(schema = @Schema(implementation = DeleteUserResult.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteUserResult> delete(@PathVariable("id") String id) {
        DeleteUserCommand command = new DeleteUserCommand(id);
        DeleteUserResult result = deleteUserUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }
}
