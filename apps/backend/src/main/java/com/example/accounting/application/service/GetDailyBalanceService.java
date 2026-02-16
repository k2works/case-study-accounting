package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetDailyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetDailyBalanceQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetDailyBalanceResult;
import com.example.accounting.application.port.out.GetDailyBalanceResult.DailyBalanceEntry;
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
public class GetDailyBalanceService implements GetDailyBalanceUseCase {

    private final AccountRepository accountRepository;
    private final JournalEntryRepository journalEntryRepository;

    public GetDailyBalanceService(AccountRepository accountRepository,
                                  JournalEntryRepository journalEntryRepository) {
        this.accountRepository = accountRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public GetDailyBalanceResult execute(GetDailyBalanceQuery query) {
        Account account = accountRepository.findById(AccountId.of(query.accountId()))
                .orElseThrow(() -> new IllegalArgumentException("勘定科目が見つかりません"));

        BigDecimal rawOpeningBalance = calculateOpeningBalance(query.accountId(), query.dateFrom());
        BigDecimal openingBalance = normalizeBalance(account.getAccountType(), rawOpeningBalance);

        List<DailyBalanceEntry> rawEntries = journalEntryRepository.findDailyBalanceByAccountAndPeriod(
                query.accountId(), query.dateFrom(), query.dateTo());

        BalanceCalculation calculation = calculateBalances(account.getAccountType(), openingBalance, rawEntries);

        return new GetDailyBalanceResult(
                account.getId().value(),
                account.getAccountCode().value(),
                account.getAccountName(),
                openingBalance,
                calculation.debitTotal(),
                calculation.creditTotal(),
                calculation.closingBalance(),
                calculation.entries()
        );
    }

    private BigDecimal calculateOpeningBalance(Integer accountId, LocalDate dateFrom) {
        if (dateFrom == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = journalEntryRepository.calculateBalanceBeforeDate(accountId, dateFrom);
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
                                                 List<DailyBalanceEntry> rawEntries) {
        record Accumulator(List<DailyBalanceEntry> entries, BigDecimal debitTotal,
                           BigDecimal creditTotal, BigDecimal running) {}

        Accumulator result = rawEntries.stream().reduce(
                new Accumulator(List.of(), BigDecimal.ZERO, BigDecimal.ZERO, openingBalance),
                (acc, entry) -> {
                    BigDecimal debitAmount = defaultAmount(entry.debitTotal());
                    BigDecimal creditAmount = defaultAmount(entry.creditTotal());
                    BigDecimal delta = accountType.isDebitBalance()
                            ? debitAmount.subtract(creditAmount)
                            : creditAmount.subtract(debitAmount);
                    BigDecimal newRunning = acc.running.add(delta);

                    var newEntry = new DailyBalanceEntry(
                            entry.date(), debitAmount, creditAmount, newRunning, entry.transactionCount());

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
            List<DailyBalanceEntry> entries,
            BigDecimal debitTotal,
            BigDecimal creditTotal,
            BigDecimal closingBalance
    ) {
    }
}
