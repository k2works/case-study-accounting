package com.example.accounting.application.service;

import com.example.accounting.application.port.in.RejectJournalEntryUseCase;
import com.example.accounting.application.port.in.command.RejectJournalEntryCommand;
import com.example.accounting.application.port.out.RejectJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class RejectJournalEntryService implements RejectJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public RejectJournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public RejectJournalEntryResult execute(RejectJournalEntryCommand command) {
        try {
            JournalEntry journalEntry = journalEntryRepository
                    .findById(new JournalEntryId(command.journalEntryId()))
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                    .orElse(null);

            if (journalEntry == null) {
                return RejectJournalEntryResult.failure("仕訳が見つかりません");
            }

            JournalEntry updated = journalEntry.reject(
                    UserId.of(command.rejectorId()),
                    LocalDateTime.now(),
                    command.rejectionReason()
            );
            journalEntryRepository.save(updated)
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

            return RejectJournalEntryResult.success(
                    updated.getId().value(),
                    updated.getStatus().name(),
                    updated.getRejectedBy().value(),
                    updated.getRejectedAt(),
                    updated.getRejectionReason()
            );
        } catch (IllegalStateException e) {
            return RejectJournalEntryResult.failure(e.getMessage());
        }
    }
}
