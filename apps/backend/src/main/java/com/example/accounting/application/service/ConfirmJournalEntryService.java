package com.example.accounting.application.service;

import com.example.accounting.application.port.in.ConfirmJournalEntryUseCase;
import com.example.accounting.application.port.in.command.ConfirmJournalEntryCommand;
import com.example.accounting.application.port.out.ConfirmJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConfirmJournalEntryService implements ConfirmJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public ConfirmJournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public ConfirmJournalEntryResult execute(ConfirmJournalEntryCommand command) {
        try {
            JournalEntry journalEntry = journalEntryRepository
                    .findById(new JournalEntryId(command.journalEntryId()))
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                    .orElse(null);

            if (journalEntry == null) {
                return ConfirmJournalEntryResult.failure("仕訳が見つかりません");
            }

            JournalEntry updated = journalEntry.confirm(UserId.of(command.confirmerId()), LocalDateTime.now());
            journalEntryRepository.save(updated)
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

            return ConfirmJournalEntryResult.success(
                    updated.getId().value(),
                    updated.getStatus().name(),
                    updated.getConfirmedBy().value(),
                    updated.getConfirmedAt()
            );
        } catch (IllegalStateException e) {
            return ConfirmJournalEntryResult.failure(e.getMessage());
        }
    }
}
