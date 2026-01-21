package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仕訳登録コントローラ
 */
@RestController
@RequestMapping("/api/journal-entries")
@Tag(name = "仕訳", description = "仕訳に関する API")
public class JournalEntryController {

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final UserRepository userRepository;

    public JournalEntryController(CreateJournalEntryUseCase createJournalEntryUseCase,
                                  UserRepository userRepository) {
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.userRepository = userRepository;
    }

    /**
     * 仕訳登録
     */
    @Operation(
            summary = "仕訳登録",
            description = "経理担当者以上が仕訳を登録します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "登録成功",
            content = @Content(schema = @Schema(implementation = CreateJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "登録失敗",
            content = @Content(schema = @Schema(implementation = CreateJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<CreateJournalEntryResponse> create(
            @Valid @RequestBody CreateJournalEntryRequest request,
            Principal principal
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new BusinessException("ユーザーが存在しません"));

        List<CreateJournalEntryCommand.JournalEntryLineInput> lines = request.lines().stream()
                .map(line -> new CreateJournalEntryCommand.JournalEntryLineInput(
                        line.lineNumber(),
                        line.accountId(),
                        line.debitAmount(),
                        line.creditAmount()
                ))
                .toList();

        CreateJournalEntryCommand command = new CreateJournalEntryCommand(
                request.journalDate(),
                request.description(),
                user.getId().value(),
                lines
        );

        CreateJournalEntryResult result = createJournalEntryUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(CreateJournalEntryResponse.success(
                    result.journalEntryId(),
                    result.journalDate(),
                    result.description(),
                    result.status()
            ));
        }
        return ResponseEntity.badRequest().body(CreateJournalEntryResponse.failure(result.errorMessage()));
    }
}
