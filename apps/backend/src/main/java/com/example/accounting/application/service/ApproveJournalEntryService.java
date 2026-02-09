package com.example.accounting.application.service;

import com.example.accounting.application.port.in.ApproveJournalEntryUseCase;
import com.example.accounting.application.port.in.command.ApproveJournalEntryCommand;
import com.example.accounting.application.port.out.ApproveJournalEntryResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.user.UserId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ApproveJournalEntryService implements ApproveJournalEntryUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public ApproveJournalEntryService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public ApproveJournalEntryResult execute(ApproveJournalEntryCommand command) {
        try {
            JournalEntry journalEntry = journalEntryRepository
                    .findById(new JournalEntryId(command.journalEntryId()))
                    .orElse(null);

            if (journalEntry == null) {
                return ApproveJournalEntryResult.failure("仕訳が見つかりません");
            }

            JournalEntry updated = journalEntry.approve(UserId.of(command.approverId()), LocalDateTime.now());
            journalEntryRepository.save(updated);

            return ApproveJournalEntryResult.success(
                    updated.getId().value(),
                    updated.getStatus().name(),
                    updated.getApprovedBy().value(),
                    updated.getApprovedAt()
            );
        } catch (IllegalStateException e) {
            return ApproveJournalEntryResult.failure(e.getMessage());
        }
    }
}
