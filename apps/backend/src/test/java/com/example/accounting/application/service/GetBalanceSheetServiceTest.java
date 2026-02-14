package com.example.accounting.application.service;

import com.example.accounting.application.port.in.query.GetBalanceSheetQuery;
import com.example.accounting.application.port.out.BalanceSheetRepository;
import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetEntry;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("貸借対照表サービス")
class GetBalanceSheetServiceTest {

    @Mock
    private BalanceSheetRepository balanceSheetRepository;

    private GetBalanceSheetService service;

    @BeforeEach
    void setUp() {
        service = new GetBalanceSheetService(balanceSheetRepository);
    }

    @Test
    @DisplayName("資産合計 = 負債合計 + 純資産合計 のとき balanced が true")
    void shouldReturnBalancedTrueWhenAssetsEqualLiabilitiesPlusEquity() {
        BalanceSheetEntity cash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        BalanceSheetEntity ap = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));
        BalanceSheetEntity capital = createEntity("300", "資本金", "EQUITY",
                BigDecimal.ZERO, new BigDecimal("2000"), new BigDecimal("-2000"));

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(cash, ap, capital));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.balanced()).isTrue();
        assertThat(result.totalAssets()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.totalLiabilities()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(result.totalEquity()).isEqualByComparingTo(new BigDecimal("2000"));
        assertThat(result.totalLiabilitiesAndEquity()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(result.difference()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("資産合計 != 負債合計 + 純資産合計 のとき balanced が false")
    void shouldReturnBalancedFalseWhenNotEqual() {
        BalanceSheetEntity cash = createEntity("100", "現金", "ASSET",
                new BigDecimal("6000"), BigDecimal.ZERO, new BigDecimal("6000"));
        BalanceSheetEntity ap = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));
        BalanceSheetEntity capital = createEntity("300", "資本金", "EQUITY",
                BigDecimal.ZERO, new BigDecimal("2000"), new BigDecimal("-2000"));

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(cash, ap, capital));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.balanced()).isFalse();
        assertThat(result.difference()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    @DisplayName("セクションが ASSET, LIABILITY, EQUITY の3つ生成される")
    void shouldBuildThreeSections() {
        BalanceSheetEntity cash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        BalanceSheetEntity ap = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));
        BalanceSheetEntity capital = createEntity("300", "資本金", "EQUITY",
                BigDecimal.ZERO, new BigDecimal("2000"), new BigDecimal("-2000"));

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(cash, ap, capital));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.sections()).hasSize(3);
        assertThat(result.sections().get(0).sectionType()).isEqualTo("ASSET");
        assertThat(result.sections().get(0).sectionDisplayName()).isEqualTo("資産の部");
        assertThat(result.sections().get(1).sectionType()).isEqualTo("LIABILITY");
        assertThat(result.sections().get(1).sectionDisplayName()).isEqualTo("負債の部");
        assertThat(result.sections().get(2).sectionType()).isEqualTo("EQUITY");
        assertThat(result.sections().get(2).sectionDisplayName()).isEqualTo("純資産の部");
    }

    @Test
    @DisplayName("セクション内のエントリが正しい金額を持つ")
    void shouldCalculateEntryAmountsCorrectly() {
        BalanceSheetEntity cash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        BalanceSheetEntity ap = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(cash, ap));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        BalanceSheetSection assetSection = result.sections().get(0);
        assertThat(assetSection.entries()).hasSize(1);
        // ASSET: balance は正のまま表示
        assertThat(assetSection.entries().get(0).amount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(assetSection.subtotal()).isEqualByComparingTo(new BigDecimal("5000"));

        BalanceSheetSection liabilitySection = result.sections().get(1);
        assertThat(liabilitySection.entries()).hasSize(1);
        // LIABILITY: balance(-3000)を反転して3000で表示
        assertThat(liabilitySection.entries().get(0).amount()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(liabilitySection.subtotal()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    @DisplayName("前期比較データが正しく計算される")
    void shouldCalculateComparativeData() {
        LocalDate currentDate = LocalDate.of(2026, 3, 31);
        LocalDate prevDate = LocalDate.of(2025, 3, 31);

        BalanceSheetEntity currentCash = createEntity("100", "現金", "ASSET",
                new BigDecimal("8000"), BigDecimal.ZERO, new BigDecimal("8000"));
        BalanceSheetEntity currentAp = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("5000"), new BigDecimal("-5000"));
        BalanceSheetEntity currentCapital = createEntity("300", "資本金", "EQUITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));

        BalanceSheetEntity prevCash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        BalanceSheetEntity prevAp = createEntity("200", "買掛金", "LIABILITY",
                BigDecimal.ZERO, new BigDecimal("3000"), new BigDecimal("-3000"));
        BalanceSheetEntity prevCapital = createEntity("300", "資本金", "EQUITY",
                BigDecimal.ZERO, new BigDecimal("2000"), new BigDecimal("-2000"));

        when(balanceSheetRepository.findBalanceSheet(currentDate))
                .thenReturn(List.of(currentCash, currentAp, currentCapital));
        when(balanceSheetRepository.findBalanceSheet(prevDate))
                .thenReturn(List.of(prevCash, prevAp, prevCapital));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(currentDate, prevDate));

        assertThat(result.comparativeDate()).isEqualTo(prevDate);

        // 資産セクションの比較データ
        BalanceSheetSection assetSection = result.sections().get(0);
        BalanceSheetEntry cashEntry = assetSection.entries().get(0);
        assertThat(cashEntry.comparative()).isNotNull();
        assertThat(cashEntry.comparative().previousAmount()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(cashEntry.comparative().difference()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(cashEntry.comparative().changeRate()).isEqualByComparingTo(new BigDecimal("60.00"));

        // セクション小計の比較データ
        assertThat(assetSection.comparativeSubtotal()).isNotNull();
        assertThat(assetSection.comparativeSubtotal().previousAmount()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    @DisplayName("前期比較日が null の場合は比較データなし")
    void shouldNotIncludeComparativeDataWhenNoPreviousDate() {
        BalanceSheetEntity cash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(cash));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.comparativeDate()).isNull();
        BalanceSheetEntry entry = result.sections().get(0).entries().get(0);
        assertThat(entry.comparative()).isNull();
        assertThat(result.sections().get(0).comparativeSubtotal()).isNull();
    }

    @Test
    @DisplayName("データがない場合は空の結果を返す")
    void shouldReturnEmptyResultWhenNoData() {
        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of());

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.sections()).hasSize(3);
        assertThat(result.totalAssets()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalLiabilities()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalEquity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.balanced()).isTrue();
    }

    @Test
    @DisplayName("指定日の貸借対照表を取得できる")
    void shouldExecuteQueryWithSpecifiedDate() {
        LocalDate date = LocalDate.of(2026, 1, 31);
        when(balanceSheetRepository.findBalanceSheet(date)).thenReturn(List.of());

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(date, null));

        assertThat(result.date()).isEqualTo(date);
    }

    @Test
    @DisplayName("前期にだけ存在する勘定の増減率を正しく計算する")
    void shouldHandleAccountOnlyInPreviousPeriod() {
        LocalDate currentDate = LocalDate.of(2026, 3, 31);
        LocalDate prevDate = LocalDate.of(2025, 3, 31);

        // 当期にはデータなし
        when(balanceSheetRepository.findBalanceSheet(currentDate)).thenReturn(List.of());

        BalanceSheetEntity prevCash = createEntity("100", "現金", "ASSET",
                new BigDecimal("5000"), BigDecimal.ZERO, new BigDecimal("5000"));
        when(balanceSheetRepository.findBalanceSheet(prevDate)).thenReturn(List.of(prevCash));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(currentDate, prevDate));

        // 当期にデータがないので entries は空
        BalanceSheetSection assetSection = result.sections().get(0);
        assertThat(assetSection.entries()).isEmpty();
    }

    @Test
    @DisplayName("null の balance を 0 として扱う")
    void shouldHandleNullBalanceAsZero() {
        BalanceSheetEntity entityWithNullBalance = new BalanceSheetEntity();
        entityWithNullBalance.setAccountCode("100");
        entityWithNullBalance.setAccountName("現金");
        entityWithNullBalance.setAccountType("ASSET");
        entityWithNullBalance.setTotalDebit(BigDecimal.ZERO);
        entityWithNullBalance.setTotalCredit(BigDecimal.ZERO);
        entityWithNullBalance.setBalance(null);

        when(balanceSheetRepository.findBalanceSheet(null)).thenReturn(List.of(entityWithNullBalance));

        GetBalanceSheetResult result = service.execute(new GetBalanceSheetQuery(null, null));

        assertThat(result.sections().get(0).entries()).hasSize(1);
        assertThat(result.sections().get(0).entries().get(0).amount())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    private BalanceSheetEntity createEntity(String code, String name, String type,
                                             BigDecimal debit, BigDecimal credit, BigDecimal balance) {
        BalanceSheetEntity entity = new BalanceSheetEntity();
        entity.setAccountCode(code);
        entity.setAccountName(name);
        entity.setAccountType(type);
        entity.setTotalDebit(debit);
        entity.setTotalCredit(credit);
        entity.setBalance(balance);
        return entity;
    }
}
