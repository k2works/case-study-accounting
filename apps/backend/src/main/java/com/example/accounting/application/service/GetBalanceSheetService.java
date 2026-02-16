package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetBalanceSheetQuery;
import com.example.accounting.application.port.in.query.GetBalanceSheetUseCase;
import com.example.accounting.application.port.out.BalanceSheetRepository;
import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetEntry;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
import com.example.accounting.application.port.out.GetBalanceSheetResult.ComparativeData;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetBalanceSheetService implements GetBalanceSheetUseCase {
    private static final List<String> SECTION_ORDER = List.of("ASSET", "LIABILITY", "EQUITY");
    private static final Map<String, String> SECTION_DISPLAY_NAMES = Map.of(
            "ASSET", "資産の部",
            "LIABILITY", "負債の部",
            "EQUITY", "純資産の部"
    );

    private final BalanceSheetRepository balanceSheetRepository;

    public GetBalanceSheetService(BalanceSheetRepository balanceSheetRepository) {
        this.balanceSheetRepository = balanceSheetRepository;
    }

    @Override
    public GetBalanceSheetResult execute(GetBalanceSheetQuery query) {
        List<BalanceSheetEntity> currentEntities = balanceSheetRepository.findBalanceSheet(query.date())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        List<BalanceSheetEntity> comparativeEntities = query.comparativeDate() != null
                ? balanceSheetRepository.findBalanceSheet(query.comparativeDate())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                : List.of();

        Map<String, BigDecimal> comparativeAmounts = buildComparativeAmountMap(comparativeEntities);

        List<BalanceSheetSection> sections = buildSections(currentEntities, comparativeAmounts,
                query.comparativeDate() != null);

        BigDecimal totalAssets = getSectionSubtotal(sections, "ASSET");
        BigDecimal totalLiabilities = getSectionSubtotal(sections, "LIABILITY");
        BigDecimal totalEquity = getSectionSubtotal(sections, "EQUITY");
        BigDecimal totalLiabilitiesAndEquity = totalLiabilities.add(totalEquity);

        boolean balanced = totalAssets.compareTo(totalLiabilitiesAndEquity) == 0;
        BigDecimal difference = totalAssets.subtract(totalLiabilitiesAndEquity).abs();

        return new GetBalanceSheetResult(
                query.date(),
                query.comparativeDate(),
                sections,
                totalAssets,
                totalLiabilities,
                totalEquity,
                totalLiabilitiesAndEquity,
                balanced,
                difference
        );
    }

    private List<BalanceSheetSection> buildSections(List<BalanceSheetEntity> entities,
                                                     Map<String, BigDecimal> comparativeAmounts,
                                                     boolean hasComparative) {
        Map<String, List<BalanceSheetEntity>> grouped = entities.stream()
                .collect(Collectors.groupingBy(BalanceSheetEntity::getAccountType));

        return SECTION_ORDER.stream()
                .map(sectionType -> buildOneSection(sectionType, grouped, comparativeAmounts, hasComparative))
                .toList();
    }

    private BalanceSheetSection buildOneSection(String sectionType,
                                                Map<String, List<BalanceSheetEntity>> grouped,
                                                Map<String, BigDecimal> comparativeAmounts,
                                                boolean hasComparative) {
        List<BalanceSheetEntity> group = grouped.getOrDefault(sectionType, List.of());
        List<BalanceSheetEntry> entries = group.stream()
                .map(e -> toEntry(e, comparativeAmounts, hasComparative))
                .toList();
        BigDecimal subtotal = entries.stream()
                .map(BalanceSheetEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ComparativeData comparativeSubtotal = buildComparativeSubtotal(entries, subtotal, hasComparative);

        return new BalanceSheetSection(
                sectionType,
                SECTION_DISPLAY_NAMES.getOrDefault(sectionType, sectionType),
                entries,
                subtotal,
                comparativeSubtotal
        );
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private ComparativeData buildComparativeSubtotal(List<BalanceSheetEntry> entries,
                                                      BigDecimal subtotal,
                                                      boolean hasComparative) {
        if (!hasComparative) {
            return null;
        }
        BigDecimal prevSubtotal = entries.stream()
                .map(e -> e.comparative() != null ? e.comparative().previousAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return buildComparativeData(subtotal, prevSubtotal);
    }

    private BalanceSheetEntry toEntry(BalanceSheetEntity entity,
                                      Map<String, BigDecimal> comparativeAmounts,
                                      boolean hasComparative) {
        BigDecimal amount = toDisplayAmount(entity);
        return new BalanceSheetEntry(
                entity.getAccountCode(),
                entity.getAccountName(),
                entity.getAccountType(),
                amount,
                hasComparative
                        ? buildComparativeData(amount, comparativeAmounts.getOrDefault(entity.getAccountCode(), BigDecimal.ZERO))
                        : null
        );
    }

    private BigDecimal toDisplayAmount(BalanceSheetEntity entity) {
        BigDecimal balance = entity.getBalance() != null ? entity.getBalance() : BigDecimal.ZERO;
        boolean isDebit = isDebitBalanceAccount(entity.getAccountType());
        // ASSET: 借方残高（正の値を表示）
        // LIABILITY/EQUITY: 貸方残高（符号を反転して正の値を表示）
        return isDebit ? balance : balance.negate();
    }

    private Map<String, BigDecimal> buildComparativeAmountMap(List<BalanceSheetEntity> entities) {
        return entities.stream()
                .collect(Collectors.toMap(
                        BalanceSheetEntity::getAccountCode,
                        this::toDisplayAmount,
                        (a, b) -> a
                ));
    }

    private ComparativeData buildComparativeData(BigDecimal currentAmount, BigDecimal previousAmount) {
        BigDecimal diff = currentAmount.subtract(previousAmount);
        BigDecimal changeRate = BigDecimal.ZERO;
        if (previousAmount.compareTo(BigDecimal.ZERO) != 0) {
            changeRate = diff
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previousAmount.abs(), 2, RoundingMode.HALF_UP);
        }
        return new ComparativeData(previousAmount, diff, changeRate);
    }

    private boolean isDebitBalanceAccount(String accountType) {
        try {
            AccountType type = AccountType.valueOf(accountType);
            return type.isDebitBalance();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private BigDecimal getSectionSubtotal(List<BalanceSheetSection> sections, String sectionType) {
        return sections.stream()
                .filter(s -> s.sectionType().equals(sectionType))
                .map(BalanceSheetSection::subtotal)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
