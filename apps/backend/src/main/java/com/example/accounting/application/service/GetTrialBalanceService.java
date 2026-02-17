package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetTrialBalanceQuery;
import com.example.accounting.application.port.in.query.GetTrialBalanceUseCase;
import com.example.accounting.application.port.out.GetTrialBalanceResult;
import com.example.accounting.application.port.out.GetTrialBalanceResult.CategorySubtotal;
import com.example.accounting.application.port.out.GetTrialBalanceResult.TrialBalanceEntry;
import com.example.accounting.application.port.out.TrialBalanceRepository;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetTrialBalanceService implements GetTrialBalanceUseCase {
    private final TrialBalanceRepository trialBalanceRepository;

    public GetTrialBalanceService(TrialBalanceRepository trialBalanceRepository) {
        this.trialBalanceRepository = trialBalanceRepository;
    }

    @Override
    public GetTrialBalanceResult execute(GetTrialBalanceQuery query) {
        List<TrialBalanceEntity> entities = trialBalanceRepository.findTrialBalance(query.date())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        List<TrialBalanceEntry> entries = entities.stream()
                .map(this::toEntry)
                .toList();

        BigDecimal totalDebit = entries.stream()
                .map(TrialBalanceEntry::debitBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = entries.stream()
                .map(TrialBalanceEntry::creditBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        boolean balanced = totalDebit.compareTo(totalCredit) == 0;
        BigDecimal difference = totalDebit.subtract(totalCredit).abs();

        List<CategorySubtotal> categorySubtotals = calculateCategorySubtotals(entries);

        return new GetTrialBalanceResult(
                query.date(),
                totalDebit,
                totalCredit,
                balanced,
                difference,
                entries,
                categorySubtotals
        );
    }

    private TrialBalanceEntry toEntry(TrialBalanceEntity entity) {
        BigDecimal balance = defaultAmount(entity.getBalance());
        String accountTypeStr = entity.getAccountType();
        boolean isDebit = isDebitBalanceAccount(accountTypeStr);
        BigDecimal[] debitCredit = splitBalance(balance, isDebit);

        return new TrialBalanceEntry(
                entity.getAccountCode(),
                entity.getAccountName(),
                entity.getBsplCategory(),
                accountTypeStr,
                debitCredit[0],
                debitCredit[1]
        );
    }

    private BigDecimal[] splitBalance(BigDecimal balance, boolean isDebitAccount) {
        boolean positiveToDebit = isDebitAccount
                ? balance.compareTo(BigDecimal.ZERO) >= 0
                : balance.compareTo(BigDecimal.ZERO) > 0;

        if (positiveToDebit) {
            return new BigDecimal[]{balance.abs(), BigDecimal.ZERO};
        }
        return new BigDecimal[]{BigDecimal.ZERO, balance.abs()};
    }

    private boolean isDebitBalanceAccount(String accountType) {
        try {
            AccountType type = AccountType.valueOf(accountType);
            return type.isDebitBalance();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private List<CategorySubtotal> calculateCategorySubtotals(List<TrialBalanceEntry> entries) {
        List<String> orderedTypes = List.of("ASSET", "LIABILITY", "EQUITY", "REVENUE", "EXPENSE");
        Map<String, String> displayNames = Map.of(
                "ASSET", "資産",
                "LIABILITY", "負債",
                "EQUITY", "純資産",
                "REVENUE", "収益",
                "EXPENSE", "費用"
        );

        Map<String, List<TrialBalanceEntry>> grouped = entries.stream()
                .collect(Collectors.groupingBy(TrialBalanceEntry::accountType));

        return orderedTypes.stream()
                .map(type -> {
                    List<TrialBalanceEntry> group = grouped.getOrDefault(type, List.of());
                    BigDecimal debitSubtotal = group.stream()
                            .map(TrialBalanceEntry::debitBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal creditSubtotal = group.stream()
                            .map(TrialBalanceEntry::creditBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CategorySubtotal(
                            type,
                            displayNames.getOrDefault(type, type),
                            debitSubtotal,
                            creditSubtotal
                    );
                })
                .toList();
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
