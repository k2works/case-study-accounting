package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountUseCase;
import com.example.accounting.application.port.in.DeleteAccountCommand;
import com.example.accounting.application.port.in.DeleteAccountUseCase;
import com.example.accounting.application.port.in.UpdateAccountUseCase;
import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.in.command.UpdateAccountCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateAccountResult;
import com.example.accounting.application.port.out.UpdateAccountResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.web.dto.AccountResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAccountResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAccountRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 勘定科目登録コントローラ
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "勘定科目", description = "勘定科目に関する API")
public class AccountController {

    private final CreateAccountUseCase createAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final AccountRepository accountRepository;

    public AccountController(CreateAccountUseCase createAccountUseCase,
                             UpdateAccountUseCase updateAccountUseCase,
                             DeleteAccountUseCase deleteAccountUseCase,
                             AccountRepository accountRepository) {
        this.createAccountUseCase = createAccountUseCase;
        this.updateAccountUseCase = updateAccountUseCase;
        this.deleteAccountUseCase = deleteAccountUseCase;
        this.accountRepository = accountRepository;
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

    /**
     * 勘定科目一覧取得
     */
    @Operation(
            summary = "勘定科目一覧取得",
            description = "認証済みユーザーが勘定科目一覧を取得します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AccountResponse>> findAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {
        Optional<AccountType> accountType = parseAccountType(type);
        Optional<String> normalizedKeyword = normalizeKeyword(keyword);

        List<Account> accounts = fetchAccounts(accountType, normalizedKeyword);

        List<AccountResponse> responses = accounts.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    private Optional<AccountType> parseAccountType(String type) {
        if (type == null || type.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(AccountType.fromCode(type));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private Optional<String> normalizeKeyword(String keyword) {
        return (keyword != null && !keyword.isBlank()) ? Optional.of(keyword) : Optional.empty();
    }

    private List<Account> fetchAccounts(Optional<AccountType> accountType, Optional<String> keyword) {
        if (accountType.isPresent() || keyword.isPresent()) {
            return accountRepository.search(accountType.orElse(null), keyword.orElse(null))
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        }
        return accountRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
    }

    /**
     * 勘定科目単体取得
     */
    @Operation(
            summary = "勘定科目単体取得",
            description = "認証済みユーザーが勘定科目を単体取得します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(schema = @Schema(implementation = AccountResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "勘定科目が存在しない",
            content = @Content
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AccountResponse> findById(@PathVariable("id") Integer id) {
        return accountRepository.findById(AccountId.of(id))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .map(account -> ResponseEntity.ok(toResponse(account)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * 勘定科目更新
     */
    @Operation(
            summary = "勘定科目更新",
            description = "管理者または経理責任者が勘定科目を更新します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(schema = @Schema(implementation = UpdateAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "更新失敗",
            content = @Content(schema = @Schema(implementation = UpdateAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UpdateAccountResponse> update(@PathVariable("id") Integer id,
                                                        @Valid @RequestBody UpdateAccountRequest request) {
        UpdateAccountCommand command = new UpdateAccountCommand(
                id,
                request.accountName(),
                request.accountType()
        );
        UpdateAccountResult result = updateAccountUseCase.execute(command);

        if (result.success()) {
            return ResponseEntity.ok(UpdateAccountResponse.success(
                    result.accountId(),
                    result.accountCode(),
                    result.accountName(),
                    result.accountType(),
                    result.message()
            ));
        }
        return ResponseEntity.badRequest().body(UpdateAccountResponse.failure(result.errorMessage()));
    }

    /**
     * 勘定科目削除
     */
    @Operation(
            summary = "勘定科目削除",
            description = "指定されたIDの勘定科目を削除します"
    )
    @ApiResponse(
            responseCode = "200",
            description = "削除成功",
            content = @Content(schema = @Schema(implementation = DeleteAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "勘定科目が存在しない",
            content = @Content(schema = @Schema(implementation = DeleteAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "使用中の勘定科目",
            content = @Content(schema = @Schema(implementation = DeleteAccountResponse.class))
    )
    @ApiResponse(
            responseCode = "403",
            description = "権限不足",
            content = @Content
    )
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DeleteAccountResponse> deleteAccount(@PathVariable Integer id) {
        DeleteAccountCommand command = new DeleteAccountCommand(id);
        var result = deleteAccountUseCase.execute(command);
        DeleteAccountResponse response = DeleteAccountResponse.from(result);

        if (result.success()) {
            return ResponseEntity.ok(response);
        }
        if ("勘定科目が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if ("この勘定科目は仕訳で使用されているため削除できません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    private AccountResponse toResponse(Account account) {
        Integer accountId = account.getId() != null ? account.getId().value() : null;
        String accountCode = account.getAccountCode() != null ? account.getAccountCode().value() : null;
        String accountType = account.getAccountType() != null ? account.getAccountType().name() : null;
        return new AccountResponse(accountId, accountCode, account.getAccountName(), accountType);
    }
}
