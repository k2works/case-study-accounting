package com.example.accounting.application.service;

import com.example.accounting.application.port.in.SubmitForApprovalUseCase;
import com.example.accounting.application.port.in.command.SubmitForApprovalCommand;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.SubmitForApprovalResult;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubmitForApprovalService implements SubmitForApprovalUseCase {

    private final JournalEntryRepository journalEntryRepository;

    public SubmitForApprovalService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    @Transactional
    public SubmitForApprovalResult execute(SubmitForApprovalCommand command) {
        try {
            JournalEntry journalEntry = journalEntryRepository
                    .findById(new JournalEntryId(command.journalEntryId()))
                    .orElse(null);

            if (journalEntry == null) {
                return SubmitForApprovalResult.failure("仕訳が見つかりません");
            }

            JournalEntry updated = journalEntry.submitForApproval();
            journalEntryRepository.save(updated);

            return SubmitForApprovalResult.success(
                    updated.getId().value(),
                    updated.getStatus().name()
            );
        } catch (IllegalStateException e) {
            return SubmitForApprovalResult.failure(e.getMessage());
        }
    }
}
