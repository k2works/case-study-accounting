package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetGeneralLedgerUseCase;
import com.example.accounting.application.port.in.query.GetGeneralLedgerQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetGeneralLedgerResult;
import com.example.accounting.application.port.out.GetGeneralLedgerResult.GeneralLedgerEntry;
import com.example.accounting.application.port.out.JournalEntryRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountId;
import com.example.accounting.domain.model.account.AccountType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GetGeneralLedgerService implements GetGeneralLedgerUseCase {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public GetGeneralLedgerService(AccountRepository accountRepository,
                                   JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GetGeneralLedgerResult execute(GetGeneralLedgerQuery query) {
        Account account = accountRepository.findById(AccountId.of(query.accountId()))
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                .orElseThrow(() -> new IllegalArgumentException("勘定科目が見つかりません"));

        int offset = query.page() * query.size();
        long totalElements = journalEntryRepository.countPostedLinesByAccountAndPeriod(
                query.accountId(), query.dateFrom(), query.dateTo())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        BigDecimal rawOpeningBalance = calculateOpeningBalance(query.accountId(), query.dateFrom());
        BigDecimal openingBalance = normalizeBalance(account.getAccountType(), rawOpeningBalance);

        List<GeneralLedgerEntry> rawEntries = totalElements == 0
                ? List.of()
                : journalEntryRepository.findPostedLinesByAccountAndPeriod(
                query.accountId(), query.dateFrom(), query.dateTo(), offset, query.size())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        BalanceCalculation calculation = calculateBalances(account.getAccountType(), openingBalance, rawEntries);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / query.size());

        return new GetGeneralLedgerResult(
                calculation.entries(),
                account.getId().value(),
                account.getAccountCode().value(),
                account.getAccountName(),
                openingBalance,
                calculation.debitTotal(),
                calculation.creditTotal(),
                calculation.closingBalance(),
                query.page(),
                query.size(),
                totalElements,
                totalPages
        );
    }

    private BigDecimal calculateOpeningBalance(Integer accountId, LocalDate dateFrom) {
        if (dateFrom == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = journalEntryRepository.calculateBalanceBeforeDate(accountId, dateFrom)
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
        return balance == null ? BigDecimal.ZERO : balance;
    }

    private BigDecimal normalizeBalance(AccountType accountType, BigDecimal balance) {
        if (accountType.isDebitBalance()) {
            return balance;
        }
        return balance.negate();
    }

    private BalanceCalculation calculateBalances(AccountType accountType,
                                                 BigDecimal openingBalance,
                                                 List<GeneralLedgerEntry> rawEntries) {
        record Accumulator(List<GeneralLedgerEntry> entries, BigDecimal debitTotal,
                           BigDecimal creditTotal, BigDecimal running) {}

        Accumulator result = rawEntries.stream().reduce(
                new Accumulator(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, openingBalance),
                (acc, entry) -> {
                    BigDecimal debitAmount = defaultAmount(entry.debitAmount());
                    BigDecimal creditAmount = defaultAmount(entry.creditAmount());
                    BigDecimal delta = accountType.isDebitBalance()
                            ? debitAmount.subtract(creditAmount)
                            : creditAmount.subtract(debitAmount);
                    BigDecimal newRunning = acc.running.add(delta);

                    var newEntry = new GeneralLedgerEntry(
                            entry.journalEntryId(), entry.journalDate(), entry.description(),
                            debitAmount, creditAmount, newRunning);

                    return new Accumulator(
                            Stream.concat(acc.entries.stream(), Stream.of(newEntry)).toList(),
                            acc.debitTotal.add(debitAmount),
                            acc.creditTotal.add(creditAmount),
                            newRunning);
                },
                (a, b) -> b);

        return new BalanceCalculation(result.entries(), result.debitTotal(), result.creditTotal(), result.running());
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private record BalanceCalculation(
            List<GeneralLedgerEntry> entries,
            BigDecimal debitTotal,
            BigDecimal creditTotal,
            BigDecimal closingBalance
    ) {
    }
}
