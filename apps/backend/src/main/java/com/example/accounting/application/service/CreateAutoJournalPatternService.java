package com.example.accounting.application.service;

import com.example.accounting.application.port.in.CreateAutoJournalPatternUseCase;
import com.example.accounting.application.port.in.command.CreateAutoJournalPatternCommand;
import com.example.accounting.application.port.out.AutoJournalPatternRepository;
import com.example.accounting.application.port.out.CreateAutoJournalPatternResult;
import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternItem;
import com.example.accounting.domain.shared.IO;
import io.vavr.control.Either;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateAutoJournalPatternService implements CreateAutoJournalPatternUseCase {

    private final AutoJournalPatternRepository autoJournalPatternRepository;

    public CreateAutoJournalPatternService(AutoJournalPatternRepository autoJournalPatternRepository) {
        this.autoJournalPatternRepository = autoJournalPatternRepository;
    }

    @Override
    public CreateAutoJournalPatternResult execute(CreateAutoJournalPatternCommand command) {
        return buildCreateProgram(command).unsafeRun();
    }

    IO<CreateAutoJournalPatternResult> buildCreateProgram(CreateAutoJournalPatternCommand command) {
        return validateUniquenessIO(command)
                .map(either -> either.flatMap(this::createPatternFromCommand))
                .flatMap(this::processCreateResult);
    }

    private IO<Either<String, CreateAutoJournalPatternCommand>> validateUniquenessIO(CreateAutoJournalPatternCommand command) {
        return IO.delay(() -> {
            if (autoJournalPatternRepository.existsByCode(command.patternCode())
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))) {
                return Either.left("パターンコードは既に使用されています");
            }
            return Either.right(command);
        });
    }

    private Either<String, AutoJournalPattern> createPatternFromCommand(CreateAutoJournalPatternCommand command) {
        try {
            AutoJournalPattern pattern = AutoJournalPattern.create(
                    command.patternCode(),
                    command.patternName(),
                    command.sourceTableName(),
                    command.description()
            );

            if (command.items() != null) {
                for (CreateAutoJournalPatternCommand.PatternItemCommand item : command.items()) {
                    pattern = pattern.addItem(AutoJournalPatternItem.create(
                            item.lineNumber(),
                            item.debitCreditType(),
                            item.accountCode(),
                            item.amountFormula(),
                            item.descriptionTemplate()
                    ));
                }
            }
            return Either.right(pattern);
        } catch (IllegalArgumentException e) {
            return Either.left(e.getMessage());
        }
    }

    private IO<CreateAutoJournalPatternResult> processCreateResult(Either<String, AutoJournalPattern> result) {
        return result.fold(
                error -> IO.pure(CreateAutoJournalPatternResult.failure(error)),
                pattern -> createPatternIO(pattern).map(this::createPatternResult)
        );
    }

    private IO<AutoJournalPattern> createPatternIO(AutoJournalPattern pattern) {
        return IO.delay(() -> autoJournalPatternRepository.save(pattern)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex)));
    }

    private CreateAutoJournalPatternResult createPatternResult(AutoJournalPattern pattern) {
        Long patternId = pattern.getId() != null ? pattern.getId().value() : null;
        return CreateAutoJournalPatternResult.success(
                patternId,
                pattern.getPatternCode(),
                pattern.getPatternName()
        );
    }
}
