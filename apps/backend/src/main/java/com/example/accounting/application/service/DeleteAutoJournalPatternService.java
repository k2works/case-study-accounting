package com.example.accounting.application.service;

import com.example.accounting.application.port.in.DeleteAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.command.DeleteAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.DeleteAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteAutoJournalPatternService implements DeleteAutoJournalPatternUseCase {

    private final AutoJournalPatternRepository autoJournalPatternRepository;

    public DeleteAutoJournalPatternService(AutoJournalPatternRepository autoJournalPatternRepository) {
        this.autoJournalPatternRepository = autoJournalPatternRepository;
    }

    @Override
    public DeleteAutoJournalPatternResult execute(DeleteAutoJournalPatternCommand command) {
        return buildDeleteProgram(command).unsafeRun();
    }

    IO<DeleteAutoJournalPatternResult> buildDeleteProgram(DeleteAutoJournalPatternCommand command) {
        return findPatternIO(command.patternId())
                .flatMap(this::processDeleteResult);
    }

    private IO<Either<String, AutoJournalPattern>> findPatternIO(Long patternId) {
        return IO.delay(() -> autoJournalPatternRepository.findById(AutoJournalPatternId.of(patternId))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, AutoJournalPattern>>map(Either::right)
                .orElseGet(() -> Either.left("自動仕訳パターンが見つかりません")));
    }

    private IO<DeleteAutoJournalPatternResult> processDeleteResult(Either<String, AutoJournalPattern> result) {
        return result.fold(
                error -> IO.pure(DeleteAutoJournalPatternResult.failure(error)),
                pattern -> deletePatternIO(pattern.getId())
                        .map(ignored -> DeleteAutoJournalPatternResult.success(pattern.getId().value()))
        );
    }

    private IO<Void> deletePatternIO(AutoJournalPatternId patternId) {
        return IO.effect(() -> autoJournalPatternRepository.deleteById(patternId)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }
}
