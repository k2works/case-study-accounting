package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteJournalEntryUseCase;
import com.example.accounting.application.port.in.command.DeleteJournalEntryCommand;
import com.example.accounting.application.port.out.DeleteJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryStatus;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 仕訳削除サービス（DeleteJournalEntryUseCase の実装）
 *
 * <p>Vavr の Either と IO モナドを使用した関数型スタイルで
 * エラーハンドリングと副作用管理を行う。</p>
 */
@Service
@Transactional
public class DeleteJournalEntryService implements DeleteJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public DeleteJournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    /**
     * 仕訳削除を実行する
     *
     * @param command 仕訳削除コマンド
     * @return 仕訳削除結果
     */
    @Override
    public DeleteJournalEntryResult execute(DeleteJournalEntryCommand command) {
        return buildDeleteProgram(command).unsafeRun();
    }

    IO<DeleteJournalEntryResult> buildDeleteProgram(DeleteJournalEntryCommand command) {
        return findJournalEntryIO(command.journalEntryId())
                .map(either -> either.flatMap(this::validateStatusForDelete))
                .flatMap(this::processDeleteResult);
    }

    private IO<Either<String, JournalEntry>> findJournalEntryIO(Integer journalEntryId) {
        return IO.delay(() -> journalEntryRepository.findById(JournalEntryId.of(journalEntryId))
                .<Either<String, JournalEntry>>map(Either::right)
                .orElseGet(() -> Either.left("仕訳が見つかりません")));
    }

    private Either<String, JournalEntry> validateStatusForDelete(JournalEntry journalEntry) {
        if (journalEntry.getStatus() != JournalEntryStatus.DRAFT) {
            return Either.left("下書き状態の仕訳のみ削除できます");
        }
        return Either.right(journalEntry);
    }

    private IO<DeleteJournalEntryResult> processDeleteResult(Either<String, JournalEntry> result) {
        return result.fold(
                error -> IO.pure(DeleteJournalEntryResult.ofFailure(error)),
                journalEntry -> deleteJournalEntryIO(journalEntry.getId())
                        .map(ignored -> DeleteJournalEntryResult.ofSuccess())
        );
    }

    private IO<Void> deleteJournalEntryIO(JournalEntryId journalEntryId) {
        return IO.effect(() -> journalEntryRepository.deleteById(journalEntryId));
    }
}
