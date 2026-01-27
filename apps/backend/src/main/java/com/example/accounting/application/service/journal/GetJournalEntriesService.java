package com.example.accounting.application.service.journal;

import com.example.accounting.application.port.in.GetJournalEntriesUseCase;
import com.example.accounting.application.port.in.query.GetJournalEntriesQuery;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult.JournalEntrySummary;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.journal.JournalEntry;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetJournalEntriesService implements GetJournalEntriesUseCase {
    private final JournalEntryRepository journalEntryRepository;

    public GetJournalEntriesService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GetJournalEntriesResult execute(GetJournalEntriesQuery query) {
        int offset = query.page() * query.size();
        long totalElements = journalEntryRepository.countByConditions(
                query.statuses(), query.dateFrom(), query.dateTo());
        if (totalElements == 0) {
            return GetJournalEntriesResult.empty(query.page(), query.size());
        }

        List<JournalEntry> journalEntries =
                journalEntryRepository.findByConditions(
                        query.statuses(), query.dateFrom(), query.dateTo(), offset, query.size());
        List<JournalEntrySummary> summaries =
                journalEntries.stream()
                        .map(
                                journalEntry ->
                                        new JournalEntrySummary(
                                                journalEntry.getId().value(),
                                                journalEntry.getJournalDate(),
                                                journalEntry.getDescription(),
                                                journalEntry.totalDebitAmount().value(),
                                                journalEntry.totalCreditAmount().value(),
                                                journalEntry.getStatus().name(),
                                                journalEntry.getVersion()))
                        .toList();
        int totalPages = (int) Math.ceil((double) totalElements / query.size());

        return new GetJournalEntriesResult(
                summaries,
                query.page(),
                query.size(),
                totalElements,
                totalPages);
    }
}
