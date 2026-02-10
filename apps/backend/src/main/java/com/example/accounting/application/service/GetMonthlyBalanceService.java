package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetMonthlyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetMonthlyBalanceQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult.MonthlyBalanceEntry;
import com.example.accounting.application.port.out.MonthlyAccountBalanceRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetMonthlyBalanceService implements GetMonthlyBalanceUseCase {

    private final AccountRepository accountRepository;
    private final MonthlyAccountBalanceRepository monthlyAccountBalanceRepository;

    public GetMonthlyBalanceService(AccountRepository accountRepository,
                                     MonthlyAccountBalanceRepository monthlyAccountBalanceRepository) {
        this.accountRepository = accountRepository;
        this.monthlyAccountBalanceRepository = monthlyAccountBalanceRepository;
    }

    @Override
    public GetMonthlyBalanceResult execute(GetMonthlyBalanceQuery query) {
        Account account = accountRepository.findByCode(query.accountCode())
                .orElseThrow(() -> new IllegalArgumentException("勘定科目が見つかりません: " + query.accountCode()));

        List<MonthlyAccountBalanceEntity> entities =
                monthlyAccountBalanceRepository.findByAccountCodeAndFiscalPeriod(
                        query.accountCode(), query.fiscalPeriod());

        List<MonthlyBalanceEntry> entries = entities.stream()
                .map(e -> new MonthlyBalanceEntry(
                        e.getMonth(),
                        defaultAmount(e.getOpeningBalance()),
                        defaultAmount(e.getDebitAmount()),
                        defaultAmount(e.getCreditAmount()),
                        defaultAmount(e.getClosingBalance())
                ))
                .toList();

        BigDecimal debitTotal = entries.stream()
                .map(MonthlyBalanceEntry::debitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditTotal = entries.stream()
                .map(MonthlyBalanceEntry::creditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal openingBalance = entries.isEmpty() ? BigDecimal.ZERO
                : entries.getFirst().openingBalance();
        BigDecimal closingBalance = entries.isEmpty() ? BigDecimal.ZERO
                : entries.getLast().closingBalance();

        return new GetMonthlyBalanceResult(
                account.getAccountCode().value(),
                account.getAccountName(),
                query.fiscalPeriod(),
                openingBalance,
                debitTotal,
                creditTotal,
                closingBalance,
                entries
        );
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
