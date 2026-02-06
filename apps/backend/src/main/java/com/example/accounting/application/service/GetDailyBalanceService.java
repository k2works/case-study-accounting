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
import java.util.ArrayList;
import java.util.List;

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
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;
        BigDecimal running = openingBalance;
        List<DailyBalanceEntry> entries = new ArrayList<>();

        for (DailyBalanceEntry entry : rawEntries) {
            BigDecimal debitAmount = defaultAmount(entry.debitTotal());
            BigDecimal creditAmount = defaultAmount(entry.creditTotal());
            debitTotal = debitTotal.add(debitAmount);
            creditTotal = creditTotal.add(creditAmount);

            BigDecimal delta = accountType.isDebitBalance()
                    ? debitAmount.subtract(creditAmount)
                    : creditAmount.subtract(debitAmount);
            running = running.add(delta);

            entries.add(new DailyBalanceEntry(
                    entry.date(),
                    debitAmount,
                    creditAmount,
                    running,
                    entry.transactionCount()
            ));
        }

        BigDecimal closingBalance = running;

        return new BalanceCalculation(entries, debitTotal, creditTotal, closingBalance);
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
