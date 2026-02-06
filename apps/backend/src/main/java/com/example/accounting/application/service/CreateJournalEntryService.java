package com.example.accounting.application.service;

import com.example.accounting.application.port.in.CreateJournalEntryUseCase;
import com.example.accounting.application.port.in.command.CreateJournalEntryCommand;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.CreateJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import com.example.accounting.domain.model.user.UserId;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 仕訳登録サービス（CreateJournalEntryUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class CreateJournalEntryService implements CreateJournalEntryUseCase {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public CreateJournalEntryService(AccountRepository accountRepository,
                                     JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    /**
     * 仕訳登録を実行する
     *
     * @param command 仕訳登録コマンド
     * @return 仕訳登録結果
     */
    @Override
    public CreateJournalEntryResult execute(CreateJournalEntryCommand command) {
        return buildCreateProgram(command).unsafeRun();
    }

    IO<CreateJournalEntryResult> buildCreateProgram(CreateJournalEntryCommand command) {
        return validateLinesIO(command)
                .map(either -> either.flatMap(this::validateAccountsExistence))
                .map(either -> either.flatMap(this::createJournalEntryFromCommand))
                .map(either -> either.flatMap(this::validateJournalEntry))
                .flatMap(this::processCreateResult);
    }

    private IO<Either<String, CreateJournalEntryCommand>> validateLinesIO(CreateJournalEntryCommand command) {
        return IO.delay(() -> {
            if (command.lines() == null || command.lines().isEmpty()) {
                return Either.left("仕訳明細は 1 行以上必要です");
            }
            return Either.right(command);
        });
    }

    private Either<String, CreateJournalEntryCommand> validateAccountsExistence(CreateJournalEntryCommand command) {
        for (CreateJournalEntryCommand.JournalEntryLineInput line : command.lines()) {
            Either<String, AccountId> validationResult = validateAccountLine(line);
            if (validationResult.isLeft()) {
                return Either.left(validationResult.getLeft());
            }
        }
        return Either.right(command);
    }

    private Either<String, AccountId> validateAccountLine(CreateJournalEntryCommand.JournalEntryLineInput line) {
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

    private Either<String, JournalEntry> createJournalEntryFromCommand(CreateJournalEntryCommand command) {
        try {
            JournalEntry entry = JournalEntry.create(
                    command.journalDate(),
                    command.description(),
                    UserId.of(command.createdByUserId()),
                    0
            );

            JournalEntry updated = entry;
            for (CreateJournalEntryCommand.JournalEntryLineInput line : command.lines()) {
                updated = updated.addLine(toJournalEntryLine(line));
            }
            return Either.right(updated);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private JournalEntryLine toJournalEntryLine(CreateJournalEntryCommand.JournalEntryLineInput line) {
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

    private IO<CreateJournalEntryResult> processCreateResult(Either<String, JournalEntry> result) {
        return result.fold(
                error -> IO.pure(CreateJournalEntryResult.failure(error)),
                journalEntry -> createJournalEntryIO(journalEntry).map(this::createJournalEntryResult)
        );
    }

    private IO<JournalEntry> createJournalEntryIO(JournalEntry journalEntry) {
        return IO.delay(() -> journalEntryRepository.save(journalEntry));
    }

    private CreateJournalEntryResult createJournalEntryResult(JournalEntry journalEntry) {
        return CreateJournalEntryResult.success(
                journalEntry.getId().value(),
                journalEntry.getJournalDate(),
                journalEntry.getDescription(),
                journalEntry.getStatus().name()
        );
    }
}
