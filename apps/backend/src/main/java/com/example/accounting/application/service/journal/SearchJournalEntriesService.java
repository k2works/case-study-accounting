package com.example.accounting.application.service.journal;

import com.example.accounting.application.port.in.SearchJournalEntriesUseCase;
import com.example.accounting.application.port.in.query.SearchJournalEntriesQuery;
import com.example.accounting.application.port.out.GetJournalEntriesResult;
import com.example.accounting.application.port.out.GetJournalEntriesResult.JournalEntrySummary;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.application.port.out.JournalEntrySearchCriteria;
import com.example.accounting.domain.model.journal.JournalEntry;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchJournalEntriesService implements SearchJournalEntriesUseCase {
    private final JournalEntryRepository journalEntryRepository;

    public SearchJournalEntriesService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GetJournalEntriesResult execute(SearchJournalEntriesQuery query) {
        int offset = query.page() * query.size();
        JournalEntrySearchCriteria criteria = new JournalEntrySearchCriteria(
                query.statuses(), query.dateFrom(), query.dateTo(),
                query.accountId(), query.amountFrom(), query.amountTo(),
                query.description(), offset, query.size());

        long totalElements = journalEntryRepository.countBySearchConditions(criteria)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        if (totalElements == 0) {
            return GetJournalEntriesResult.empty(query.page(), query.size());
        }

        List<JournalEntry> journalEntries =
                journalEntryRepository.searchByConditions(criteria)
                        .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        List<JournalEntrySummary> summaries =
                journalEntries.stream()
                        .map(journalEntry ->
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
