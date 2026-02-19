package com.example.accounting.application.service;

import com.example.accounting.application.port.in.UpdateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.in.command.UpdateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.UpdateAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UpdateAutoJournalPatternService implements UpdateAutoJournalPatternUseCase {

    private final AutoJournalPatternRepository autoJournalPatternRepository;

    public UpdateAutoJournalPatternService(AutoJournalPatternRepository autoJournalPatternRepository) {
        this.autoJournalPatternRepository = autoJournalPatternRepository;
    }

    @Override
    public UpdateAutoJournalPatternResult execute(UpdateAutoJournalPatternCommand command) {
        return buildUpdateProgram(command).unsafeRun();
    }

    IO<UpdateAutoJournalPatternResult> buildUpdateProgram(UpdateAutoJournalPatternCommand command) {
        return findPatternIO(command.patternId())
                .map(either -> either.flatMap(pattern -> updatePatternFromCommand(command, pattern)))
                .flatMap(this::processUpdateResult);
    }

    private IO<Either<String, AutoJournalPattern>> findPatternIO(Long patternId) {
        return IO.delay(() -> autoJournalPatternRepository.findById(AutoJournalPatternId.of(patternId))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .<Either<String, AutoJournalPattern>>map(Either::right)
                .orElseGet(() -> Either.left("自動仕訳パターンが見つかりません")));
    }

    private Either<String, AutoJournalPattern> updatePatternFromCommand(UpdateAutoJournalPatternCommand command,
                                                                         AutoJournalPattern pattern) {
        try {
            List<AutoJournalPatternItem> items = command.items() == null
                    ? List.of()
                    : command.items().stream()
                    .map(this::toPatternItem)
                    .toList();

            AutoJournalPattern updated = pattern
                    .withPatternName(command.patternName())
                    .withSourceTableName(command.sourceTableName())
                    .withDescription(command.description())
                    .withIsActive(command.isActive())
                    .withItems(items);

            return Either.right(updated);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private AutoJournalPatternItem toPatternItem(CreateAutoJournalPatternCommand.PatternItemCommand item) {
        return AutoJournalPatternItem.create(
                item.lineNumber(),
                item.debitCreditType(),
                item.accountCode(),
                item.amountFormula(),
                item.descriptionTemplate()
        );
    }

    private IO<UpdateAutoJournalPatternResult> processUpdateResult(Either<String, AutoJournalPattern> result) {
        return result.fold(
                error -> IO.pure(UpdateAutoJournalPatternResult.failure(error)),
                pattern -> updatePatternIO(pattern).map(this::updatePatternResult)
        );
    }

    private IO<AutoJournalPattern> updatePatternIO(AutoJournalPattern pattern) {
        return IO.delay(() -> autoJournalPatternRepository.save(pattern)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private UpdateAutoJournalPatternResult updatePatternResult(AutoJournalPattern pattern) {
        return UpdateAutoJournalPatternResult.success(
                pattern.getId().value(),
                pattern.getPatternCode(),
                pattern.getPatternName(),
                "自動仕訳パターンを更新しました"
        );
    }
}
