package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.DeleteAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.UpdateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.DeleteAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.UpdateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.CreateAutoJournalPatternResult;
import com.example.accounting.application.port.out.UpdateAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.infrastructure.web.dto.AutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.CreateAutoJournalPatternRequest;
import com.example.accounting.infrastructure.web.dto.CreateAutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAutoJournalPatternResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAutoJournalPatternRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAutoJournalPatternResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auto-journal-patterns")
@Tag(name = "自動仕訳パターン", description = "自動仕訳パターンに関する API")
public class AutoJournalPatternController {

    private final CreateAutoJournalPatternUseCase createAutoJournalPatternUseCase;
    private final UpdateAutoJournalPatternUseCase updateAutoJournalPatternUseCase;
    private final DeleteAutoJournalPatternUseCase deleteAutoJournalPatternUseCase;
    private final AutoJournalPatternRepository autoJournalPatternRepository;

    public AutoJournalPatternController(CreateAutoJournalPatternUseCase createAutoJournalPatternUseCase,
                                        UpdateAutoJournalPatternUseCase updateAutoJournalPatternUseCase,
                                        DeleteAutoJournalPatternUseCase deleteAutoJournalPatternUseCase,
                                        AutoJournalPatternRepository autoJournalPatternRepository) {
        this.createAutoJournalPatternUseCase = createAutoJournalPatternUseCase;
        this.updateAutoJournalPatternUseCase = updateAutoJournalPatternUseCase;
        this.deleteAutoJournalPatternUseCase = deleteAutoJournalPatternUseCase;
        this.autoJournalPatternRepository = autoJournalPatternRepository;
    }

    @Operation(summary = "自動仕訳パターン一覧取得", description = "認証済みユーザーが自動仕訳パターン一覧を取得します")
    @ApiResponse(responseCode = "200", description = "取得成功",
            content = @Content(schema = @Schema(implementation = AutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "403", description = "権限不足", content = @Content)
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AutoJournalPatternResponse>> findAll() {
        List<AutoJournalPattern> patterns = autoJournalPatternRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        List<AutoJournalPatternResponse> responses = patterns.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "自動仕訳パターン単体取得", description = "認証済みユーザーが自動仕訳パターンを単体取得します")
    @ApiResponse(responseCode = "200", description = "取得成功",
            content = @Content(schema = @Schema(implementation = AutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "404", description = "自動仕訳パターンが存在しない", content = @Content)
    @ApiResponse(responseCode = "403", description = "権限不足", content = @Content)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AutoJournalPatternResponse> findById(@PathVariable("id") Long id) {
        return autoJournalPatternRepository.findById(AutoJournalPatternId.of(id))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .map(pattern -> ResponseEntity.ok(toResponse(pattern)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "自動仕訳パターン登録", description = "管理者または経理責任者が自動仕訳パターンを登録します")
    @ApiResponse(responseCode = "200", description = "登録成功",
            content = @Content(schema = @Schema(implementation = CreateAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "400", description = "登録失敗",
            content = @Content(schema = @Schema(implementation = CreateAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "403", description = "権限不足", content = @Content)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateAutoJournalPatternResponse> create(
            @Valid @RequestBody CreateAutoJournalPatternRequest request) {
        CreateAutoJournalPatternCommand command = new CreateAutoJournalPatternCommand(
                request.patternCode(),
                request.patternName(),
                request.sourceTableName(),
                request.description(),
                request.items().stream()
                        .map(item -> new CreateAutoJournalPatternCommand.PatternItemCommand(
                                item.lineNumber(),
                                item.debitCreditType(),
                                item.accountCode(),
                                item.amountFormula(),
                                item.descriptionTemplate()
                        ))
                        .toList()
        );

        CreateAutoJournalPatternResult result = createAutoJournalPatternUseCase.execute(command);
        if (result.success()) {
            return ResponseEntity.ok(CreateAutoJournalPatternResponse.success(
                    result.patternId(),
                    result.patternCode(),
                    result.patternName()
            ));
        }
        return ResponseEntity.badRequest().body(CreateAutoJournalPatternResponse.failure(result.errorMessage()));
    }

    @Operation(summary = "自動仕訳パターン更新", description = "管理者または経理責任者が自動仕訳パターンを更新します")
    @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(schema = @Schema(implementation = UpdateAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "400", description = "更新失敗",
            content = @Content(schema = @Schema(implementation = UpdateAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "403", description = "権限不足", content = @Content)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UpdateAutoJournalPatternResponse> update(@PathVariable("id") Long id,
                                                                   @Valid @RequestBody UpdateAutoJournalPatternRequest request) {
        UpdateAutoJournalPatternCommand command = new UpdateAutoJournalPatternCommand(
                id,
                request.patternName(),
                request.sourceTableName(),
                request.description(),
                request.isActive(),
                request.items().stream()
                        .map(item -> new CreateAutoJournalPatternCommand.PatternItemCommand(
                                item.lineNumber(),
                                item.debitCreditType(),
                                item.accountCode(),
                                item.amountFormula(),
                                item.descriptionTemplate()
                        ))
                        .toList()
        );

        UpdateAutoJournalPatternResult result = updateAutoJournalPatternUseCase.execute(command);
        if (result.success()) {
            return ResponseEntity.ok(UpdateAutoJournalPatternResponse.success(
                    result.patternId(),
                    result.patternCode(),
                    result.patternName(),
                    result.message()
            ));
        }
        return ResponseEntity.badRequest().body(UpdateAutoJournalPatternResponse.failure(result.errorMessage()));
    }

    @Operation(summary = "自動仕訳パターン削除", description = "指定された ID の自動仕訳パターンを削除します")
    @ApiResponse(responseCode = "200", description = "削除成功",
            content = @Content(schema = @Schema(implementation = DeleteAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "404", description = "自動仕訳パターンが存在しない",
            content = @Content(schema = @Schema(implementation = DeleteAutoJournalPatternResponse.class)))
    @ApiResponse(responseCode = "403", description = "権限不足", content = @Content)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DeleteAutoJournalPatternResponse> delete(@PathVariable("id") Long id) {
        DeleteAutoJournalPatternCommand command = new DeleteAutoJournalPatternCommand(id);
        var result = deleteAutoJournalPatternUseCase.execute(command);
        DeleteAutoJournalPatternResponse response = DeleteAutoJournalPatternResponse.from(result);

        if (result.success()) {
            return ResponseEntity.ok(response);
        }
        if ("自動仕訳パターンが見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    private AutoJournalPatternResponse toResponse(AutoJournalPattern pattern) {
        Long patternId = pattern.getId() != null ? pattern.getId().value() : null;
        List<AutoJournalPatternResponse.ItemResponse> items = pattern.getItems().stream()
                .map(item -> new AutoJournalPatternResponse.ItemResponse(
                        item.getLineNumber(),
                        item.getDebitCreditType(),
                        item.getAccountCode(),
                        item.getAmountFormula(),
                        item.getDescriptionTemplate()
                ))
                .toList();

        return new AutoJournalPatternResponse(
                patternId,
                pattern.getPatternCode(),
                pattern.getPatternName(),
                pattern.getSourceTableName(),
                pattern.getDescription(),
                pattern.getIsActive(),
                items
        );
    }
}
