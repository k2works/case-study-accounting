package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.CreateAccountStructureUseCase;
import com.example.accounting.application.port.in.DeleteAccountStructureUseCase;
import com.example.accounting.application.port.in.UpdateAccountStructureUseCase;
import com.example.accounting.application.port.in.command.CreateAccountStructureCommand;
import com.example.accounting.application.port.in.command.DeleteAccountStructureCommand;
import com.example.accounting.application.port.in.command.UpdateAccountStructureCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.AccountStructureRepository;
import com.example.accounting.application.port.out.CreateAccountStructureResult;
import com.example.accounting.application.port.out.DeleteAccountStructureResult;
import com.example.accounting.application.port.out.UpdateAccountStructureResult;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountStructure;
import com.example.accounting.infrastructure.web.dto.AccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.CreateAccountStructureRequest;
import com.example.accounting.infrastructure.web.dto.CreateAccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.DeleteAccountStructureResponse;
import com.example.accounting.infrastructure.web.dto.UpdateAccountStructureRequest;
import com.example.accounting.infrastructure.web.dto.UpdateAccountStructureResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/account-structures")
@Tag(name = "勘定科目構成", description = "勘定科目構成に関する API")
public class AccountStructureController {

    private final CreateAccountStructureUseCase createAccountStructureUseCase;
    private final UpdateAccountStructureUseCase updateAccountStructureUseCase;
    private final DeleteAccountStructureUseCase deleteAccountStructureUseCase;
    private final AccountStructureRepository accountStructureRepository;
    private final AccountRepository accountRepository;

    public AccountStructureController(CreateAccountStructureUseCase createAccountStructureUseCase,
                                      UpdateAccountStructureUseCase updateAccountStructureUseCase,
                                      DeleteAccountStructureUseCase deleteAccountStructureUseCase,
                                      AccountStructureRepository accountStructureRepository,
                                      AccountRepository accountRepository) {
        this.createAccountStructureUseCase = createAccountStructureUseCase;
        this.updateAccountStructureUseCase = updateAccountStructureUseCase;
        this.deleteAccountStructureUseCase = deleteAccountStructureUseCase;
        this.accountStructureRepository = accountStructureRepository;
        this.accountRepository = accountRepository;
    }

    @Operation(summary = "勘定科目構成一覧取得", description = "勘定科目構成の一覧を取得します")
    @ApiResponse(responseCode = "200", description = "取得成功",
            content = @Content(schema = @Schema(implementation = AccountStructureResponse.class)))
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<List<AccountStructureResponse>> findAll() {
        List<AccountStructure> structures = accountStructureRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        Map<String, String> accountNameMap = accountRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .stream()
                .collect(Collectors.toMap(account -> account.getAccountCode().value(), Account::getAccountName));

        List<AccountStructureResponse> responses = structures.stream()
                .map(structure -> toResponse(structure, accountNameMap))
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "勘定科目構成単体取得", description = "指定コードの勘定科目構成を取得します")
    @ApiResponse(responseCode = "200", description = "取得成功",
            content = @Content(schema = @Schema(implementation = AccountStructureResponse.class)))
    @ApiResponse(responseCode = "404", description = "勘定科目構成が存在しない", content = @Content)
    @GetMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AccountStructureResponse> findByCode(@PathVariable("code") String code) {
        Map<String, String> accountNameMap = accountRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .stream()
                .collect(Collectors.toMap(account -> account.getAccountCode().value(), Account::getAccountName));

        return accountStructureRepository.findByCode(code)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .map(structure -> ResponseEntity.ok(toResponse(structure, accountNameMap)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "勘定科目構成登録", description = "勘定科目構成を登録します")
    @ApiResponse(responseCode = "200", description = "登録成功",
            content = @Content(schema = @Schema(implementation = CreateAccountStructureResponse.class)))
    @ApiResponse(responseCode = "400", description = "登録失敗",
            content = @Content(schema = @Schema(implementation = CreateAccountStructureResponse.class)))
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<CreateAccountStructureResponse> create(
            @Valid @RequestBody CreateAccountStructureRequest request
    ) {
        CreateAccountStructureCommand command = new CreateAccountStructureCommand(
                request.accountCode(),
                request.parentAccountCode(),
                request.displayOrder()
        );

        CreateAccountStructureResult result = createAccountStructureUseCase.execute(command);
        if (result.success()) {
            return ResponseEntity.ok(CreateAccountStructureResponse.success(
                    result.accountCode(),
                    result.accountPath(),
                    result.hierarchyLevel(),
                    result.parentAccountCode(),
                    result.displayOrder()
            ));
        }
        return ResponseEntity.badRequest().body(CreateAccountStructureResponse.failure(result.errorMessage()));
    }

    @Operation(summary = "勘定科目構成更新", description = "勘定科目構成を更新します")
    @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(schema = @Schema(implementation = UpdateAccountStructureResponse.class)))
    @ApiResponse(responseCode = "400", description = "更新失敗",
            content = @Content(schema = @Schema(implementation = UpdateAccountStructureResponse.class)))
    @PutMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<UpdateAccountStructureResponse> update(
            @PathVariable("code") String code,
            @Valid @RequestBody UpdateAccountStructureRequest request
    ) {
        UpdateAccountStructureCommand command = new UpdateAccountStructureCommand(
                code,
                request.parentAccountCode(),
                request.displayOrder()
        );

        UpdateAccountStructureResult result = updateAccountStructureUseCase.execute(command);
        if (result.success()) {
            return ResponseEntity.ok(UpdateAccountStructureResponse.success(
                    result.accountCode(),
                    result.accountPath(),
                    result.hierarchyLevel(),
                    result.parentAccountCode(),
                    result.displayOrder(),
                    result.message()
            ));
        }
        return ResponseEntity.badRequest().body(UpdateAccountStructureResponse.failure(result.errorMessage()));
    }

    @Operation(summary = "勘定科目構成削除", description = "勘定科目構成を削除します")
    @ApiResponse(responseCode = "200", description = "削除成功",
            content = @Content(schema = @Schema(implementation = DeleteAccountStructureResponse.class)))
    @ApiResponse(responseCode = "404", description = "勘定科目構成が存在しない",
            content = @Content(schema = @Schema(implementation = DeleteAccountStructureResponse.class)))
    @ApiResponse(responseCode = "409", description = "子階層が存在",
            content = @Content(schema = @Schema(implementation = DeleteAccountStructureResponse.class)))
    @DeleteMapping("/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DeleteAccountStructureResponse> delete(@PathVariable("code") String code) {
        DeleteAccountStructureCommand command = new DeleteAccountStructureCommand(code);
        DeleteAccountStructureResult result = deleteAccountStructureUseCase.execute(command);
        DeleteAccountStructureResponse response = DeleteAccountStructureResponse.from(result);

        if (result.success()) {
            return ResponseEntity.ok(response);
        }
        if ("勘定科目構成が見つかりません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if ("子階層が存在するため削除できません".equals(result.errorMessage())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    private AccountStructureResponse toResponse(AccountStructure structure, Map<String, String> accountNameMap) {
        return new AccountStructureResponse(
                structure.getAccountCode(),
                accountNameMap.getOrDefault(structure.getAccountCode(), null),
                structure.getAccountPath(),
                structure.getHierarchyLevel(),
                structure.getParentAccountCode(),
                structure.getDisplayOrder()
        );
    }
}
