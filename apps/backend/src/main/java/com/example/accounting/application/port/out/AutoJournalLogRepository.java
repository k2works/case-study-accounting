package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.auto_journal.AutoJournalLog;
import io.vavr.control.Try;

public interface AutoJournalLogRepository {
    Try<AutoJournalLog> save(AutoJournalLog log);
}
