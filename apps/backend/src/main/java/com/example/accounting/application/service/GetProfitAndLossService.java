package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetProfitAndLossQuery;
import com.example.accounting.application.port.in.query.GetProfitAndLossUseCase;
import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ComparativeData;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossEntry;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossSection;
import com.example.accounting.application.port.out.ProfitAndLossRepository;
import com.example.accounting.infrastructure.persistence.entity.ProfitAndLossEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetProfitAndLossService implements GetProfitAndLossUseCase {
    private static final List<String> SECTION_ORDER = List.of("REVENUE", "EXPENSE");
    private static final Map<String, String> SECTION_DISPLAY_NAMES = Map.of(
            "REVENUE", "収益の部",
            "EXPENSE", "費用の部"
    );

    private final ProfitAndLossRepository profitAndLossRepository;

    public GetProfitAndLossService(ProfitAndLossRepository profitAndLossRepository) {
        this.profitAndLossRepository = profitAndLossRepository;
    }

    @Override
    public GetProfitAndLossResult execute(GetProfitAndLossQuery query) {
        List<ProfitAndLossEntity> currentEntities = profitAndLossRepository
                .findProfitAndLoss(query.dateFrom(), query.dateTo())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        boolean hasComparative = query.comparativeDateFrom() != null || query.comparativeDateTo() != null;

        List<ProfitAndLossEntity> comparativeEntities = hasComparative
                ? profitAndLossRepository
                .findProfitAndLoss(query.comparativeDateFrom(), query.comparativeDateTo())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex))
                : List.of();

        Map<String, BigDecimal> comparativeAmounts = buildComparativeAmountMap(comparativeEntities);

        List<ProfitAndLossSection> sections = buildSections(currentEntities, comparativeAmounts, hasComparative);

        BigDecimal totalRevenue = getSectionSubtotal(sections, "REVENUE");
        BigDecimal totalExpense = getSectionSubtotal(sections, "EXPENSE");
        BigDecimal netIncome = totalRevenue.subtract(totalExpense);

        return new GetProfitAndLossResult(
                query.dateFrom(),
                query.dateTo(),
                query.comparativeDateFrom(),
                query.comparativeDateTo(),
                sections,
                totalRevenue,
                totalExpense,
                netIncome
        );
    }

    private List<ProfitAndLossSection> buildSections(List<ProfitAndLossEntity> entities,
                                                      Map<String, BigDecimal> comparativeAmounts,
                                                      boolean hasComparative) {
        Map<String, List<ProfitAndLossEntity>> grouped = entities.stream()
                .collect(Collectors.groupingBy(ProfitAndLossEntity::getAccountType));

        return SECTION_ORDER.stream()
                .map(sectionType -> buildOneSection(sectionType, grouped, comparativeAmounts, hasComparative))
                .toList();
    }

    private ProfitAndLossSection buildOneSection(String sectionType,
                                                  Map<String, List<ProfitAndLossEntity>> grouped,
                                                  Map<String, BigDecimal> comparativeAmounts,
                                                  boolean hasComparative) {
        List<ProfitAndLossEntity> group = grouped.getOrDefault(sectionType, List.of());
        List<ProfitAndLossEntry> entries = group.stream()
                .map(e -> toEntry(e, comparativeAmounts, hasComparative))
                .toList();
        BigDecimal subtotal = entries.stream()
                .map(ProfitAndLossEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ComparativeData comparativeSubtotal = buildComparativeSubtotal(entries, subtotal, hasComparative);

        return new ProfitAndLossSection(
                sectionType,
                SECTION_DISPLAY_NAMES.getOrDefault(sectionType, sectionType),
                entries,
                subtotal,
                comparativeSubtotal
        );
    }

    @SuppressWarnings("PMD.AvoidReturningNull")
    private ComparativeData buildComparativeSubtotal(List<ProfitAndLossEntry> entries,
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

    private ProfitAndLossEntry toEntry(ProfitAndLossEntity entity,
                                        Map<String, BigDecimal> comparativeAmounts,
                                        boolean hasComparative) {
        BigDecimal amount = entity.getAmount() != null ? entity.getAmount() : BigDecimal.ZERO;
        return new ProfitAndLossEntry(
                entity.getAccountCode(),
                entity.getAccountName(),
                entity.getAccountType(),
                amount,
                hasComparative
                        ? buildComparativeData(amount, comparativeAmounts.getOrDefault(entity.getAccountCode(), BigDecimal.ZERO))
                        : null
        );
    }

    private Map<String, BigDecimal> buildComparativeAmountMap(List<ProfitAndLossEntity> entities) {
        return entities.stream()
                .collect(Collectors.toMap(
                        ProfitAndLossEntity::getAccountCode,
                        e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO,
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

    private BigDecimal getSectionSubtotal(List<ProfitAndLossSection> sections, String sectionType) {
        return sections.stream()
                .filter(s -> s.sectionType().equals(sectionType))
                .map(ProfitAndLossSection::subtotal)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
