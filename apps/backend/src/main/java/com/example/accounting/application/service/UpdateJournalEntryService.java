package com.example.accounting.application.service;

import com.example.accounting.application.port.in.UpdateJournalEntryUseCase;
import com.example.accounting.application.port.in.command.UpdateJournalEntryCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.UpdateJournalEntryResult;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 仕訳編集サービス（UpdateJournalEntryUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class UpdateJournalEntryService implements UpdateJournalEntryUseCase {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public UpdateJournalEntryService(AccountRepository accountRepository,
                                     JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    /**
     * 仕訳編集を実行する
     *
     * @param command 仕訳編集コマンド
     * @return 仕訳編集結果
     */
    @Override
    public UpdateJournalEntryResult execute(UpdateJournalEntryCommand command) {
        return buildUpdateProgram(command).unsafeRun();
    }

    IO<UpdateJournalEntryResult> buildUpdateProgram(UpdateJournalEntryCommand command) {
        return findJournalEntryIO(command.journalEntryId())
                .map(either -> either.flatMap(this::validateStatusForEdit))
                .map(either -> either.flatMap(entry -> validateVersion(entry, command.version())))
                .map(either -> either.map(entry -> new UpdateContext(entry, command)))
                .map(either -> either.flatMap(this::validateLines))
                .map(either -> either.flatMap(this::validateAccountsExistence))
                .map(either -> either.flatMap(this::updateJournalEntryFromCommand))
                .map(either -> either.flatMap(this::validateJournalEntry))
                .flatMap(this::processUpdateResult);
    }

    private IO<Either<String, JournalEntry>> findJournalEntryIO(Integer journalEntryId) {
        return IO.delay(() -> journalEntryRepository.findById(JournalEntryId.of(journalEntryId))
                .<Either<String, JournalEntry>>map(Either::right)
                .orElseGet(() -> Either.left("仕訳が見つかりません")));
    }

    private Either<String, JournalEntry> validateStatusForEdit(JournalEntry journalEntry) {
        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            return Either.left("下書き状態の仕訳のみ編集可能です");
        }
        return Either.right(journalEntry);
    }

    private Either<String, JournalEntry> validateVersion(JournalEntry journalEntry, Integer version) {
        if (!Objects.equals(journalEntry.getVersion(), version)) {
            return Either.left("仕訳のバージョンが一致しません");
        }
        return Either.right(journalEntry);
    }

    private Either<String, UpdateContext> validateLines(UpdateContext context) {
        if (context.command().lines() == null || context.command().lines().isEmpty()) {
            return Either.left("仕訳明細は 1 行以上必要です");
        }
        return Either.right(context);
    }

    private Either<String, UpdateContext> validateAccountsExistence(UpdateContext context) {
        for (UpdateJournalEntryCommand.JournalEntryLineInput line : context.command().lines()) {
            Either<String, AccountId> validationResult = validateAccountLine(line);
            if (validationResult.isLeft()) {
                return Either.left(validationResult.getLeft());
            }
        }
        return Either.right(context);
    }

    private Either<String, AccountId> validateAccountLine(UpdateJournalEntryCommand.JournalEntryLineInput line) {
        if (line.accountId() == null) {
            return Either.left("勘定科目IDは必須です");
        }
        try {
            AccountId accountId = AccountId.of(line.accountId());
            if (accountRepository.findById(accountId).isEmpty()) {
                return Either.left("勘定科目が存在しません");
            }
            return Either.right(accountId);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private Either<String, JournalEntry> updateJournalEntryFromCommand(UpdateContext context) {
        try {
            List<JournalEntryLine> newLines = context.command().lines().stream()
                    .map(this::toJournalEntryLine)
                    .toList();
            JournalEntry updated = context.entry().update(
                    context.command().journalDate(),
                    context.command().description(),
                    newLines
            );
            return Either.right(updated);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Either.left(e.getMessage());
        }
    }

    private JournalEntryLine toJournalEntryLine(UpdateJournalEntryCommand.JournalEntryLineInput line) {
        Money debitAmount = line.debitAmount() == null ? null : Money.of(line.debitAmount());
        Money creditAmount = line.creditAmount() == null ? null : Money.of(line.creditAmount());
        return JournalEntryLine.of(
                line.lineNumber(),
                AccountId.of(line.accountId()),
                debitAmount,
                creditAmount
        );
    }

    private Either<String, JournalEntry> validateJournalEntry(JournalEntry journalEntry) {
        try {
            journalEntry.validateForSave();
            return Either.right(journalEntry);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<UpdateJournalEntryResult> processUpdateResult(Either<String, JournalEntry> result) {
        return result.fold(
                error -> IO.pure(UpdateJournalEntryResult.failure(error)),
                journalEntry -> updateJournalEntryIO(journalEntry).map(this::updateJournalEntryResult)
        );
    }

    private IO<JournalEntry> updateJournalEntryIO(JournalEntry journalEntry) {
        return IO.delay(() -> journalEntryRepository.save(journalEntry));
    }

    private UpdateJournalEntryResult updateJournalEntryResult(JournalEntry journalEntry) {
        return UpdateJournalEntryResult.success(
                journalEntry.getId().value(),
                journalEntry.getJournalDate(),
                journalEntry.getDescription(),
                journalEntry.getStatus().name(),
                journalEntry.getVersion()
        );
    }

    private record UpdateContext(JournalEntry entry, UpdateJournalEntryCommand command) {
    }
}
