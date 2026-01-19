package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountUseCase;
import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.infrastructure.web.dto.CreateAccountRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
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
 * 勘定科目登録コントローラ
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "勘定科目", description = "勘定科目に関する API")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;

    public AccountController(CreateAccountUseCase createAccountUseCase) {
        this.createAccountUseCase = createAccountUseCase;
    }

    /**
     * 勘定科目登録
     */
    @Operation(
            summary = "勘定科目登録",
            description = "管理者または経理責任者が勘定科目を登録します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "登録成功",
            content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "登録失敗",
            content = @Content(schema = @Schema(implementation = CreateAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateAccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        CreateAccountCommand command = new CreateAccountCommand(
                request.accountCode(),
                request.accountName(),
                request.accountType()
        );
        CreateAccountResult result = createAccountUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(CreateAccountResponse.success(
                    result.accountId(),
                    result.accountCode(),
                    result.accountName(),
                    result.accountType()
            ));
        }
        return ResponseEntity.badRequest().body(CreateAccountResponse.failure(result.errorMessage()));
    }
}
