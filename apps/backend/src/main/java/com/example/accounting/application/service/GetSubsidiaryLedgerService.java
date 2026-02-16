package com.example.accounting.application.service;

import com.example.accounting.application.port.in.GetSubsidiaryLedgerUseCase;
import com.example.accounting.application.port.in.query.GetSubsidiaryLedgerQuery;
import com.example.accounting.application.port.out.AccountRepository;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import com.example.accounting.application.port.out.SubsidiaryLedgerRepository;
import com.example.accounting.domain.model.account.Account;
import com.example.accounting.domain.model.account.AccountType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class GetSubsidiaryLedgerService implements GetSubsidiaryLedgerUseCase {

    private final AccountRepository accountRepository;
    private final SubsidiaryLedgerRepository subsidiaryLedgerRepository;

    public GetSubsidiaryLedgerService(AccountRepository accountRepository,
                                      SubsidiaryLedgerRepository subsidiaryLedgerRepository) {
        this.accountRepository = accountRepository;
        this.subsidiaryLedgerRepository = subsidiaryLedgerRepository;
    }

    @Override
    public GetSubsidiaryLedgerResult execute(GetSubsidiaryLedgerQuery query) {
        Account account = accountRepository.findByCode(query.accountCode())
                .orElseThrow(() -> new IllegalArgumentException("勘定科目が見つかりません: " + query.accountCode()));

        int offset = query.page() * query.size();
        long totalElements = subsidiaryLedgerRepository.countPostedLinesByAccountAndSubAccountAndPeriod(
                query.accountCode(), query.subAccountCode(), query.dateFrom(), query.dateTo());

        BigDecimal rawOpeningBalance = calculateOpeningBalance(
                query.accountCode(), query.subAccountCode(), query.dateFrom());
        BigDecimal openingBalance = normalizeBalance(account.getAccountType(), rawOpeningBalance);

        List<SubsidiaryLedgerEntry> rawEntries = totalElements == 0
                ? List.of()
                : subsidiaryLedgerRepository.findPostedLinesByAccountAndSubAccountAndPeriod(
                query.accountCode(), query.subAccountCode(), query.dateFrom(), query.dateTo(), offset, query.size());

        BalanceCalculation calculation = calculateBalances(account.getAccountType(), openingBalance, rawEntries);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / query.size());

        return new GetSubsidiaryLedgerResult(
                calculation.entries(),
                account.getAccountCode().value(),
                account.getAccountName(),
                query.subAccountCode(),
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

    private BigDecimal calculateOpeningBalance(String accountCode, String subAccountCode, LocalDate dateFrom) {
        if (dateFrom == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal balance = subsidiaryLedgerRepository.calculateBalanceBeforeDateByAccountAndSubAccount(
                accountCode, subAccountCode, dateFrom);
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
                                                 List<SubsidiaryLedgerEntry> rawEntries) {
        record Accumulator(List<SubsidiaryLedgerEntry> entries, BigDecimal debitTotal,
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

                    var newEntry = new SubsidiaryLedgerEntry(
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
            List<SubsidiaryLedgerEntry> entries,
            BigDecimal debitTotal,
            BigDecimal creditTotal,
            BigDecimal closingBalance
    ) {
    }
}
