package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetJournalEntryUseCase;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.JournalEntryDetailResult;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.journal.JournalEntry;
import com.example.accounting.domain.model.journal.JournalEntryId;
import com.example.accounting.domain.model.journal.JournalEntryLine;
import com.example.accounting.domain.model.journal.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetJournalEntryService implements GetJournalEntryUseCase {

    private static final String UNKNOWN_ACCOUNT = "不明";

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    public GetJournalEntryService(JournalEntryRepository journalEntryRepository,
                                  AccountRepository accountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Optional<JournalEntryDetailResult> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        return journalEntryRepository.findById(JournalEntryId.of(id))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .map(entry -> toDetailResult(entry, loadAccountMap(entry.getLines())));
    }

    @Override
    public List<JournalEntryDetailResult> findAll() {
        List<JournalEntry> entries = journalEntryRepository.findAll()
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        Map<AccountId, Account> accountMap = loadAccountMap(
                entries.stream()
                        .flatMap(entry -> entry.getLines().stream())
                        .toList()
        );
        return entries.stream()
                .map(entry -> toDetailResult(entry, accountMap))
                .toList();
    }

    private Map<AccountId, Account> loadAccountMap(List<JournalEntryLine> lines) {
        Set<AccountId> accountIds = lines.stream()
                .map(JournalEntryLine::accountId)
                .collect(Collectors.toSet());
        return accountIds.stream()
                .map(id -> accountRepository.findById(id)
                        .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                        .map(account -> Map.entry(id, account)))
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private JournalEntryDetailResult toDetailResult(JournalEntry entry, Map<AccountId, Account> accountMap) {
        List<JournalEntryDetailResult.JournalEntryLineDetail> lines = entry.getLines().stream()
                .map(line -> toLineDetail(line, accountMap))
                .toList();
        return new JournalEntryDetailResult(
                entry.getId().value(),
                entry.getJournalDate(),
                entry.getDescription(),
                entry.getStatus().name(),
                entry.getVersion(),
                lines
        );
    }

    private JournalEntryDetailResult.JournalEntryLineDetail toLineDetail(
            JournalEntryLine line,
            Map<AccountId, Account> accountMap
    ) {
        Account account = accountMap.get(line.accountId());
        String accountCode = account == null ? UNKNOWN_ACCOUNT : account.getAccountCode().value();
        String accountName = account == null ? UNKNOWN_ACCOUNT : account.getAccountName();
        return new JournalEntryDetailResult.JournalEntryLineDetail(
                line.lineNumber(),
                line.accountId().value(),
                accountCode,
                accountName,
                moneyValue(line.debitAmount()),
                moneyValue(line.creditAmount())
        );
    }

    private BigDecimal moneyValue(Money amount) {
        return amount == null ? null : amount.value();
    }
}
