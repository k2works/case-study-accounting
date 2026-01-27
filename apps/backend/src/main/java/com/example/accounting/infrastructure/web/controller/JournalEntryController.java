package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.DeleteJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntriesUseCase;
import com.example.accounting.application.port.in.UpdateJournalEntryUseCase;
import com.example.accounting.application.port.in.query.GetJournalEntriesQuery;
import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.in.command.UpdateJournalEntryCommand;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.shared.OptimisticLockException;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.DeleteJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.JournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仕訳登録コントローラ
 */
@RestController
@RequestMapping("/api/journal-entries")
@Tag(name = "仕訳", description = "仕訳に関する API")
@SuppressWarnings("PMD.CouplingBetweenObjects") // コントローラは複数のユースケースを統合するため結合度が高くなる
public class JournalEntryController {

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final UpdateJournalEntryUseCase updateJournalEntryUseCase;
    private final GetJournalEntryUseCase getJournalEntryUseCase;
    private final DeleteJournalEntryUseCase deleteJournalEntryUseCase;
    private final GetJournalEntriesUseCase getJournalEntriesUseCase;
    private final UserRepository userRepository;

    public JournalEntryController(CreateJournalEntryUseCase createJournalEntryUseCase,
                                  UpdateJournalEntryUseCase updateJournalEntryUseCase,
                                  GetJournalEntryUseCase getJournalEntryUseCase,
                                  DeleteJournalEntryUseCase deleteJournalEntryUseCase,
                                  GetJournalEntriesUseCase getJournalEntriesUseCase,
                                  UserRepository userRepository) {
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUseCase = updateJournalEntryUseCase;
        this.getJournalEntryUseCase = getJournalEntryUseCase;
        this.deleteJournalEntryUseCase = deleteJournalEntryUseCase;
        this.getJournalEntriesUseCase = getJournalEntriesUseCase;
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

    /**
     * 仕訳一覧取得（ページネーション対応）
     */
    @Operation(
            summary = "仕訳一覧取得",
            description = "経理担当者以上が仕訳一覧を取得します（ページネーション対応）"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功"
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetJournalEntriesResult> findAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        GetJournalEntriesQuery query = new GetJournalEntriesQuery(
                page,
                size,
                status != null ? status : List.of(),
                dateFrom,
                dateTo
        );
        GetJournalEntriesResult result = getJournalEntriesUseCase.execute(query);
        return ResponseEntity.ok(result);
    }

    /**
     * 仕訳詳細取得
     */
    @Operation(
            summary = "仕訳詳細取得",
            description = "経理担当者以上が仕訳詳細を取得します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功"
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない"
    )
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<JournalEntryResponse> findById(@PathVariable Integer id) {
        return getJournalEntryUseCase.findById(id)
                .map(JournalEntryResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 仕訳編集
     */
    @Operation(
            summary = "仕訳編集",
            description = "経理担当者以上が仕訳を編集します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "編集成功",
            content = @Content(schema = @Schema(implementation = UpdateJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "編集失敗（バリデーションエラー、楽観的ロックエラー等）",
            content = @Content(schema = @Schema(implementation = UpdateJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない",
            content = @Content
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<UpdateJournalEntryResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateJournalEntryRequest request,
            Principal principal
    ) {
        List<UpdateJournalEntryCommand.JournalEntryLineInput> lines = request.lines().stream()
                .map(line -> new UpdateJournalEntryCommand.JournalEntryLineInput(
                        line.lineNumber(),
                        line.accountId(),
                        line.debitAmount(),
                        line.creditAmount()
                ))
                .toList();

        UpdateJournalEntryCommand command = new UpdateJournalEntryCommand(
                id,
                request.journalDate(),
                request.description(),
                lines,
                request.version()
        );

        try {
            UpdateJournalEntryResult result = updateJournalEntryUseCase.execute(command);

            if (result.success()) {
                return ResponseEntity.ok(UpdateJournalEntryResponse.success(
                        result.journalEntryId(),
                        result.journalDate(),
                        result.description(),
                        result.status(),
                        result.version(),
                        "仕訳を更新しました"
                ));
            }

            if ("仕訳が見つかりません".equals(result.errorMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            if ("仕訳のバージョンが一致しません".equals(result.errorMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(UpdateJournalEntryResponse.failure(result.errorMessage()));
            }
            return ResponseEntity.badRequest()
                    .body(UpdateJournalEntryResponse.failure(result.errorMessage()));
        } catch (OptimisticLockException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(UpdateJournalEntryResponse.failure(ex.getMessage()));
        }
    }

    /**
     * 仕訳削除
     */
    @Operation(
            summary = "仕訳削除",
            description = "経理担当者以上が仕訳を削除します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "削除成功",
            content = @Content(schema = @Schema(implementation = DeleteJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "削除失敗（ステータスエラー等）",
            content = @Content(schema = @Schema(implementation = DeleteJournalEntryResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない",
            content = @Content
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DeleteJournalEntryResponse> delete(@PathVariable Integer id) {
        DeleteJournalEntryCommand command = new DeleteJournalEntryCommand(id);
        DeleteJournalEntryResult result = deleteJournalEntryUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(DeleteJournalEntryResponse.success("仕訳を削除しました"));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(DeleteJournalEntryResponse.failure(result.errorMessage()));
    }
}
