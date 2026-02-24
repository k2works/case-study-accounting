package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.DeleteJournalEntryUseCase;
import com.example.accounting.application.port.in.ConfirmJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntryUseCase;
import com.example.accounting.application.port.in.GetJournalEntriesUseCase;
import com.example.accounting.application.port.in.GenerateAutoJournalUseCase;
import com.example.accounting.application.port.in.ApproveJournalEntryUseCase;
import com.example.accounting.application.port.in.RejectJournalEntryUseCase;
import com.example.accounting.application.port.in.RecordAuditLogUseCase;
import com.example.accounting.application.port.in.SearchJournalEntriesUseCase;
import com.example.accounting.application.port.in.SubmitForApprovalUseCase;
import com.example.accounting.application.port.in.UpdateJournalEntryUseCase;
import com.example.accounting.application.service.JournalEntryExportService;
import com.example.accounting.application.port.in.RecordAuditLogUseCase.RecordAuditLogCommand;
import com.example.accounting.application.port.in.query.GetJournalEntriesQuery;
import com.example.accounting.application.port.in.query.SearchJournalEntriesQuery;
import com.example.accounting.application.port.in.command.ApproveJournalEntryCommand;
import com.example.accounting.application.port.in.command.ConfirmJournalEntryCommand;
import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.in.command.GenerateAutoJournalCommand;
import com.example.accounting.application.port.in.command.RejectJournalEntryCommand;
import com.example.accounting.application.port.in.command.SubmitForApprovalCommand;
import com.example.accounting.application.port.in.command.UpdateJournalEntryCommand;
import com.example.accounting.application.port.out.ApproveJournalEntryResult;
import com.example.accounting.application.port.out.ConfirmJournalEntryResult;
import com.example.accounting.application.port.out.RejectJournalEntryResult;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.GenerateAutoJournalResult;
import com.example.accounting.application.port.out.SubmitForApprovalResult;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
import com.example.accounting.application.port.out.UserRepository;
import com.example.accounting.domain.model.audit.AuditAction;
import com.example.accounting.domain.model.audit.EntityType;
import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.shared.OptimisticLockException;
import com.example.accounting.infrastructure.web.dto.ApproveJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.ConfirmJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.RejectJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.RejectJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.CreateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.DeleteJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.JournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.SubmitForApprovalResponse;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryRequest;
import com.example.accounting.infrastructure.web.dto.UpdateJournalEntryResponse;
import com.example.accounting.infrastructure.web.dto.GenerateAutoJournalRequest;
import com.example.accounting.infrastructure.web.dto.GenerateAutoJournalResponse;
import com.example.accounting.infrastructure.web.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@SuppressWarnings({"PMD.CouplingBetweenObjects", "PMD.ExcessiveImports"}) // コントローラは複数のユースケースを統合するため結合度が高くなる
public class JournalEntryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryController.class);

    private final CreateJournalEntryUseCase createJournalEntryUseCase;
    private final UpdateJournalEntryUseCase updateJournalEntryUseCase;
    private final GetJournalEntryUseCase getJournalEntryUseCase;
    private final DeleteJournalEntryUseCase deleteJournalEntryUseCase;
    private final GetJournalEntriesUseCase getJournalEntriesUseCase;
    private final SearchJournalEntriesUseCase searchJournalEntriesUseCase;
    private final SubmitForApprovalUseCase submitForApprovalUseCase;
    private final ApproveJournalEntryUseCase approveJournalEntryUseCase;
    private final RejectJournalEntryUseCase rejectJournalEntryUseCase;
    private final ConfirmJournalEntryUseCase confirmJournalEntryUseCase;
    private final GenerateAutoJournalUseCase generateAutoJournalUseCase;
    private final RecordAuditLogUseCase recordAuditLogUseCase;
    private final UserRepository userRepository;
    private final JournalEntryExportService journalEntryExportService;

    @SuppressWarnings("java:S107") // コントローラは複数のユースケースを統合するため引数が多い
    public JournalEntryController(CreateJournalEntryUseCase createJournalEntryUseCase,
                                  UpdateJournalEntryUseCase updateJournalEntryUseCase,
                                  GetJournalEntryUseCase getJournalEntryUseCase,
                                  DeleteJournalEntryUseCase deleteJournalEntryUseCase,
                                  GetJournalEntriesUseCase getJournalEntriesUseCase,
                                  SearchJournalEntriesUseCase searchJournalEntriesUseCase,
                                  SubmitForApprovalUseCase submitForApprovalUseCase,
                                  ApproveJournalEntryUseCase approveJournalEntryUseCase,
                                  RejectJournalEntryUseCase rejectJournalEntryUseCase,
                                  ConfirmJournalEntryUseCase confirmJournalEntryUseCase,
                                  GenerateAutoJournalUseCase generateAutoJournalUseCase,
                                  RecordAuditLogUseCase recordAuditLogUseCase,
                                  UserRepository userRepository,
                                  JournalEntryExportService journalEntryExportService) {
        this.createJournalEntryUseCase = createJournalEntryUseCase;
        this.updateJournalEntryUseCase = updateJournalEntryUseCase;
        this.getJournalEntryUseCase = getJournalEntryUseCase;
        this.deleteJournalEntryUseCase = deleteJournalEntryUseCase;
        this.getJournalEntriesUseCase = getJournalEntriesUseCase;
        this.searchJournalEntriesUseCase = searchJournalEntriesUseCase;
        this.submitForApprovalUseCase = submitForApprovalUseCase;
        this.approveJournalEntryUseCase = approveJournalEntryUseCase;
        this.rejectJournalEntryUseCase = rejectJournalEntryUseCase;
        this.confirmJournalEntryUseCase = confirmJournalEntryUseCase;
        this.generateAutoJournalUseCase = generateAutoJournalUseCase;
        this.recordAuditLogUseCase = recordAuditLogUseCase;
        this.userRepository = userRepository;
        this.journalEntryExportService = journalEntryExportService;
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
            Principal principal,
            HttpServletRequest httpServletRequest
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
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
            recordAuditLogSafely(
                    resolveUserId(principal, null),
                    AuditAction.CREATE,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(result.journalEntryId()),
                    "仕訳伝票作成",
                    httpServletRequest
            );
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
     * 仕訳一覧エクスポート
     */
    @Operation(summary = "仕訳一覧エクスポート", description = "仕訳一覧を CSV または Excel 形式でエクスポートします")
    @ApiResponse(responseCode = "200", description = "エクスポート成功")
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<byte[]> exportJournalEntries(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(defaultValue = "excel") String format
    ) {
        GetJournalEntriesQuery query = new GetJournalEntriesQuery(
                0, 10000, status != null ? status : List.of(), dateFrom, dateTo);
        GetJournalEntriesResult result = getJournalEntriesUseCase.execute(query);

        if ("csv".equalsIgnoreCase(format)) {
            return journalEntryExportService.exportToCsv(result)
                    .map(bytes -> ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=journal-entries.csv")
                            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                            .body(bytes))
                    .getOrElseGet(error -> ResponseEntity.internalServerError().build());
        }

        return journalEntryExportService.exportToExcel(result)
                .map(bytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=journal-entries.xlsx")
                        .contentType(MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .body(bytes))
                .getOrElseGet(error -> ResponseEntity.internalServerError().build());
    }

    /**
     * 仕訳検索
     */
    @Operation(
            summary = "仕訳検索",
            description = "検索条件を指定して仕訳を検索します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "検索成功"
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<GetJournalEntriesResult> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) Integer accountId,
            @RequestParam(required = false) BigDecimal amountFrom,
            @RequestParam(required = false) BigDecimal amountTo,
            @RequestParam(required = false) String description
    ) {
        SearchJournalEntriesQuery query = new SearchJournalEntriesQuery(
                page,
                size,
                status != null ? status : List.of(),
                dateFrom,
                dateTo,
                accountId,
                amountFrom,
                amountTo,
                description
        );
        GetJournalEntriesResult result = searchJournalEntriesUseCase.execute(query);
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
            Principal principal,
            HttpServletRequest httpServletRequest
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
                recordAuditLogSafely(
                        resolveUserId(principal, null),
                        AuditAction.UPDATE,
                        EntityType.JOURNAL_ENTRY,
                        String.valueOf(id),
                        "仕訳伝票更新",
                        httpServletRequest
                );
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UpdateJournalEntryResponse.failure(result.errorMessage()));
        } catch (OptimisticLockException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(UpdateJournalEntryResponse.failure(ex.getMessage()));
        }
    }

    /**
     * 仕訳承認申請
     */
    @Operation(
            summary = "仕訳承認申請",
            description = "経理担当者が仕訳を承認申請します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "承認申請成功"
    )
    @ApiResponse(
            responseCode = "400",
            description = "承認申請失敗（ステータスエラー等）"
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない"
    )
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<SubmitForApprovalResponse> submitForApproval(@PathVariable Integer id,
                                                                       Principal principal,
                                                                       HttpServletRequest httpServletRequest) {
        SubmitForApprovalCommand command = new SubmitForApprovalCommand(id);
        SubmitForApprovalResult result = submitForApprovalUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(principal, null),
                    AuditAction.UPDATE,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(id),
                    "仕訳伝票承認依頼",
                    httpServletRequest
            );
            return ResponseEntity.ok(SubmitForApprovalResponse.success(
                    result.journalEntryId(),
                    result.status(),
                    result.message()
            ));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(SubmitForApprovalResponse.failure(result.errorMessage()));
    }

    /**
     * 仕訳承認
     */
    @Operation(
            summary = "仕訳承認",
            description = "管理者またはマネージャーが仕訳を承認します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "承認成功"
    )
    @ApiResponse(
            responseCode = "400",
            description = "承認失敗（ステータスエラー等）"
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない"
    )
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApproveJournalEntryResponse> approveJournalEntry(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest) {
        ApproveJournalEntryCommand command = new ApproveJournalEntryCommand(id, userDetails.getUsername());
        ApproveJournalEntryResult result = approveJournalEntryUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(null, userDetails),
                    AuditAction.APPROVE,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(id),
                    "仕訳伝票承認",
                    httpServletRequest
            );
            return ResponseEntity.ok(ApproveJournalEntryResponse.success(
                    result.journalEntryId(),
                    result.status(),
                    result.approvedBy(),
                    result.approvedAt(),
                    result.message()
            ));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(ApproveJournalEntryResponse.failure(result.errorMessage()));
    }

    /**
     * 仕訳確定
     */
    @Operation(
            summary = "仕訳確定",
            description = "管理者またはマネージャーが仕訳を確定します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "確定成功"
    )
    @ApiResponse(
            responseCode = "400",
            description = "確定失敗（ステータスエラー等）"
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない"
    )
    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ConfirmJournalEntryResponse> confirmJournalEntry(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest) {
        ConfirmJournalEntryCommand command = new ConfirmJournalEntryCommand(id, userDetails.getUsername());
        ConfirmJournalEntryResult result = confirmJournalEntryUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(null, userDetails),
                    AuditAction.CONFIRM,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(id),
                    "仕訳伝票確定",
                    httpServletRequest
            );
            return ResponseEntity.ok(ConfirmJournalEntryResponse.success(
                    result.journalEntryId(),
                    result.status(),
                    result.confirmedBy(),
                    result.confirmedAt(),
                    result.message()
            ));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(ConfirmJournalEntryResponse.failure(result.errorMessage()));
    }

    /**
     * 仕訳差し戻し
     */
    @Operation(
            summary = "仕訳差し戻し",
            description = "管理者またはマネージャーが仕訳を差し戻します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "差し戻し成功"
    )
    @ApiResponse(
            responseCode = "400",
            description = "差し戻し失敗（ステータスエラー等）"
    )
    @ApiResponse(
            responseCode = "404",
            description = "仕訳が見つからない"
    )
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RejectJournalEntryResponse> rejectJournalEntry(
            @PathVariable Integer id,
            @Valid @RequestBody RejectJournalEntryRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpServletRequest) {
        RejectJournalEntryCommand command = new RejectJournalEntryCommand(
                id, userDetails.getUsername(), request.rejectionReason());
        RejectJournalEntryResult result = rejectJournalEntryUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(null, userDetails),
                    AuditAction.REJECT,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(id),
                    "仕訳伝票差し戻し",
                    httpServletRequest
            );
            return ResponseEntity.ok(RejectJournalEntryResponse.success(
                    result.journalEntryId(),
                    result.status(),
                    result.rejectedBy(),
                    result.rejectedAt(),
                    result.rejectionReason(),
                    result.message()
            ));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(RejectJournalEntryResponse.failure(result.errorMessage()));
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
    public ResponseEntity<DeleteJournalEntryResponse> delete(@PathVariable Integer id,
                                                             Principal principal,
                                                             HttpServletRequest httpServletRequest) {
        DeleteJournalEntryCommand command = new DeleteJournalEntryCommand(id);
        DeleteJournalEntryResult result = deleteJournalEntryUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(principal, null),
                    AuditAction.DELETE,
                    EntityType.JOURNAL_ENTRY,
                    String.valueOf(id),
                    "仕訳伝票削除",
                    httpServletRequest
            );
            return ResponseEntity.ok(DeleteJournalEntryResponse.success("仕訳を削除しました"));
        }

        if ("仕訳が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.badRequest()
                .body(DeleteJournalEntryResponse.failure(result.errorMessage()));
    }

    /**
     * 自動仕訳生成
     */
    @Operation(
            summary = "自動仕訳生成",
            description = "パターンに基づいて仕訳を自動生成します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "生成成功",
            content = @Content(schema = @Schema(implementation = GenerateAutoJournalResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "生成失敗",
            content = @Content(schema = @Schema(implementation = GenerateAutoJournalResponse.class))
    )
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<GenerateAutoJournalResponse> generate(
            @Valid @RequestBody GenerateAutoJournalRequest request,
            Principal principal,
            HttpServletRequest httpServletRequest
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .orElseThrow(() -> new BusinessException("ユーザーが存在しません"));

        GenerateAutoJournalCommand command = new GenerateAutoJournalCommand(
                request.patternId(),
                request.amounts(),
                request.journalDate(),
                request.description(),
                user.getId().value()
        );

        GenerateAutoJournalResult result = generateAutoJournalUseCase.execute(command);

        if (result.success()) {
            recordAuditLogSafely(
                    resolveUserId(principal, null),
                    AuditAction.CREATE,
                    EntityType.JOURNAL_ENTRY,
                    null,
                    "自動仕訳生成",
                    httpServletRequest
            );
            return ResponseEntity.ok(GenerateAutoJournalResponse.success(
                    result.journalEntryId(),
                    result.journalDate(),
                    result.description(),
                    result.status()
            ));
        }
        return ResponseEntity.badRequest().body(GenerateAutoJournalResponse.failure(result.errorMessage()));
    }

    private String resolveUserId(Principal principal, UserDetails userDetails) {
        if (principal != null) {
            return principal.getName();
        }
        if (userDetails != null) {
            return userDetails.getUsername();
        }
        return "system";
    }

    private void recordAuditLogSafely(String userId,
                                      AuditAction actionType,
                                      EntityType entityType,
                                      String entityId,
                                      String description,
                                      HttpServletRequest httpServletRequest) {
        try {
            recordAuditLogUseCase.execute(
                    new RecordAuditLogCommand(
                            userId,
                            actionType,
                            entityType,
                            entityId,
                            description,
                            httpServletRequest.getRemoteAddr()
                    )
            );
        } catch (RuntimeException ex) {
            LOGGER.warn("監査ログ記録に失敗しました。 userId={}, actionType={}", userId, actionType, ex);
        }
    }
}
