package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.auto_journal.AutoJournalPattern;
import com.example.accounting.domain.model.auto_journal.AutoJournalPatternId;
import io.vavr.control.Try;

import java.util.List;
import java.util.Optional;

public interface AutoJournalPatternRepository {
    Try<AutoJournalPattern> save(AutoJournalPattern pattern);

    Try<Optional<AutoJournalPattern>> findById(AutoJournalPatternId id);

    Try<List<AutoJournalPattern>> findAll();

    Try<Void> deleteById(AutoJournalPatternId id);

    Try<Boolean> existsByCode(String patternCode);
}
