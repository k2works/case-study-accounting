package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetFinancialAnalysisQuery;
import com.example.accounting.application.port.in.query.GetFinancialAnalysisUseCase;
import com.example.accounting.application.port.out.BalanceSheetRepository;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.FinancialIndicator;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.IndicatorCategory;
import com.example.accounting.application.port.out.ProfitAndLossRepository;
import com.example.accounting.domain.model.account.AccountType;
import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import com.example.accounting.infrastructure.persistence.entity.ProfitAndLossEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetFinancialAnalysisService implements GetFinancialAnalysisUseCase {

    private final BalanceSheetRepository balanceSheetRepository;
    private final ProfitAndLossRepository profitAndLossRepository;

    public GetFinancialAnalysisService(
            BalanceSheetRepository balanceSheetRepository,
            ProfitAndLossRepository profitAndLossRepository) {
        this.balanceSheetRepository = balanceSheetRepository;
        this.profitAndLossRepository = profitAndLossRepository;
    }

    @Override
    public GetFinancialAnalysisResult execute(GetFinancialAnalysisQuery query) {
        List<BalanceSheetEntity> bsEntities = balanceSheetRepository
                .findBalanceSheet(query.dateTo())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        List<ProfitAndLossEntity> plEntities = profitAndLossRepository
                .findProfitAndLoss(query.dateFrom(), query.dateTo())
                .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

        BigDecimal totalAssets = sumByType(bsEntities, "ASSET");
        BigDecimal totalLiabilities = sumByType(bsEntities, "LIABILITY");
        BigDecimal totalEquity = sumByType(bsEntities, "EQUITY");

        BigDecimal totalRevenue = sumPlByType(plEntities, "REVENUE");
        BigDecimal totalExpense = sumPlByType(plEntities, "EXPENSE");
        BigDecimal netIncome = totalRevenue.subtract(totalExpense);

        boolean hasComparative = query.comparativeDateFrom() != null || query.comparativeDateTo() != null;
        BigDecimal prevTotalAssets = BigDecimal.ZERO;
        BigDecimal prevTotalLiabilities = BigDecimal.ZERO;
        BigDecimal prevTotalEquity = BigDecimal.ZERO;
        BigDecimal prevTotalRevenue = BigDecimal.ZERO;
        BigDecimal prevNetIncome = BigDecimal.ZERO;

        if (hasComparative) {
            List<BalanceSheetEntity> prevBs = balanceSheetRepository
                    .findBalanceSheet(query.comparativeDateTo())
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));
            List<ProfitAndLossEntity> prevPl = profitAndLossRepository
                    .findProfitAndLoss(query.comparativeDateFrom(), query.comparativeDateTo())
                    .getOrElseThrow(ex -> new RuntimeException("Data access error", ex));

            prevTotalAssets = sumByType(prevBs, "ASSET");
            prevTotalLiabilities = sumByType(prevBs, "LIABILITY");
            prevTotalEquity = sumByType(prevBs, "EQUITY");
            prevTotalRevenue = sumPlByType(prevPl, "REVENUE");
            BigDecimal prevTotalExpense = sumPlByType(prevPl, "EXPENSE");
            prevNetIncome = prevTotalRevenue.subtract(prevTotalExpense);
        }

        List<IndicatorCategory> categories = List.of(
                buildProfitabilityCategory(netIncome, totalEquity, totalAssets, totalRevenue,
                        prevNetIncome, prevTotalEquity, prevTotalAssets, prevTotalRevenue, hasComparative),
                buildSafetyCategory(totalAssets, totalLiabilities, totalEquity,
                        prevTotalAssets, prevTotalLiabilities, prevTotalEquity, hasComparative),
                buildEfficiencyCategory(totalRevenue, totalAssets,
                        prevTotalRevenue, prevTotalAssets, hasComparative)
        );

        return new GetFinancialAnalysisResult(
                query.dateFrom(),
                query.dateTo(),
                query.comparativeDateFrom(),
                query.comparativeDateTo(),
                categories
        );
    }

    private BigDecimal sumByType(List<BalanceSheetEntity> entities, String accountType) {
        return entities.stream()
                .filter(e -> accountType.equals(e.getAccountType()))
                .map(e -> {
                    BigDecimal balance = e.getBalance() != null ? e.getBalance() : BigDecimal.ZERO;
                    boolean isDebit = isDebitBalanceAccount(accountType);
                    return isDebit ? balance : balance.negate();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumPlByType(List<ProfitAndLossEntity> entities, String accountType) {
        return entities.stream()
                .filter(e -> accountType.equals(e.getAccountType()))
                .map(e -> e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean isDebitBalanceAccount(String accountType) {
        try {
            AccountType type = AccountType.valueOf(accountType);
            return type.isDebitBalance();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private IndicatorCategory buildProfitabilityCategory(
            BigDecimal netIncome, BigDecimal totalEquity, BigDecimal totalAssets, BigDecimal totalRevenue,
            BigDecimal prevNetIncome, BigDecimal prevTotalEquity, BigDecimal prevTotalAssets,
            BigDecimal prevTotalRevenue, boolean hasComparative) {

        BigDecimal roe = safeDividePercent(netIncome, totalEquity);
        BigDecimal prevRoe = hasComparative ? safeDividePercent(prevNetIncome, prevTotalEquity) : null;

        BigDecimal roa = safeDividePercent(netIncome, totalAssets);
        BigDecimal prevRoa = hasComparative ? safeDividePercent(prevNetIncome, prevTotalAssets) : null;

        BigDecimal netProfitMargin = safeDividePercent(netIncome, totalRevenue);
        BigDecimal prevNetProfitMargin = hasComparative ? safeDividePercent(prevNetIncome, prevTotalRevenue) : null;

        List<FinancialIndicator> indicators = List.of(
                buildIndicator("ROE（自己資本利益率）", "%", roe, prevRoe, hasComparative,
                        "当期純利益 ÷ 自己資本 × 100", new BigDecimal("8.0")),
                buildIndicator("ROA（総資産利益率）", "%", roa, prevRoa, hasComparative,
                        "当期純利益 ÷ 総資産 × 100", new BigDecimal("3.0")),
                buildIndicator("売上高利益率", "%", netProfitMargin, prevNetProfitMargin, hasComparative,
                        "当期純利益 ÷ 売上高 × 100", new BigDecimal("5.0"))
        );

        return new IndicatorCategory("PROFITABILITY", "収益性", indicators);
    }

    private IndicatorCategory buildSafetyCategory(
            BigDecimal totalAssets, BigDecimal totalLiabilities, BigDecimal totalEquity,
            BigDecimal prevTotalAssets, BigDecimal prevTotalLiabilities, BigDecimal prevTotalEquity,
            boolean hasComparative) {

        BigDecimal currentRatio = safeDividePercent(totalAssets, totalLiabilities);
        BigDecimal prevCurrentRatio = hasComparative ? safeDividePercent(prevTotalAssets, prevTotalLiabilities) : null;

        BigDecimal equityRatio = safeDividePercent(totalEquity, totalAssets);
        BigDecimal prevEquityRatio = hasComparative ? safeDividePercent(prevTotalEquity, prevTotalAssets) : null;

        BigDecimal debtRatio = safeDividePercent(totalLiabilities, totalEquity);
        BigDecimal prevDebtRatio = hasComparative ? safeDividePercent(prevTotalLiabilities, prevTotalEquity) : null;

        List<FinancialIndicator> indicators = List.of(
                buildIndicator("流動比率", "%", currentRatio, prevCurrentRatio, hasComparative,
                        "流動資産 ÷ 流動負債 × 100", new BigDecimal("200.0")),
                buildIndicator("自己資本比率", "%", equityRatio, prevEquityRatio, hasComparative,
                        "自己資本 ÷ 総資産 × 100", new BigDecimal("40.0")),
                buildIndicator("負債比率", "%", debtRatio, prevDebtRatio, hasComparative,
                        "負債 ÷ 自己資本 × 100", new BigDecimal("150.0"))
        );

        return new IndicatorCategory("SAFETY", "安全性", indicators);
    }

    private IndicatorCategory buildEfficiencyCategory(
            BigDecimal totalRevenue, BigDecimal totalAssets,
            BigDecimal prevTotalRevenue, BigDecimal prevTotalAssets,
            boolean hasComparative) {

        BigDecimal assetTurnover = safeDivide(totalRevenue, totalAssets);
        BigDecimal prevAssetTurnover = hasComparative ? safeDivide(prevTotalRevenue, prevTotalAssets) : null;

        List<FinancialIndicator> indicators = List.of(
                buildIndicator("総資産回転率", "回", assetTurnover, prevAssetTurnover, hasComparative,
                        "売上高 ÷ 総資産", new BigDecimal("1.0"))
        );

        return new IndicatorCategory("EFFICIENCY", "効率性", indicators);
    }

    private BigDecimal safeDividePercent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator
                .multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private FinancialIndicator buildIndicator(
            String name, String unit, BigDecimal value, BigDecimal previousValue,
            boolean hasComparative, String formula, BigDecimal industryAverage) {

        BigDecimal prev = hasComparative ? previousValue : null;
        BigDecimal diff = null;
        BigDecimal changeRate = null;

        if (hasComparative && previousValue != null) {
            diff = value.subtract(previousValue);
            if (previousValue.compareTo(BigDecimal.ZERO) != 0) {
                changeRate = diff
                        .multiply(BigDecimal.valueOf(100))
                        .divide(previousValue.abs(), 2, RoundingMode.HALF_UP);
            } else {
                changeRate = BigDecimal.ZERO;
            }
        }

        return new FinancialIndicator(name, unit, value, prev, diff, changeRate, formula, industryAverage);
    }
}
