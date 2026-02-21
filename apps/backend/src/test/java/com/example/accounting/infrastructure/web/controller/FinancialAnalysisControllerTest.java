package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetFinancialAnalysisQuery;
import com.example.accounting.application.port.in.query.GetFinancialAnalysisUseCase;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.FinancialIndicator;
import com.example.accounting.application.port.out.GetFinancialAnalysisResult.IndicatorCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("財務分析コントローラ")
class FinancialAnalysisControllerTest {

    @Mock
    private GetFinancialAnalysisUseCase getFinancialAnalysisUseCase;

    private FinancialAnalysisController controller;

    @BeforeEach
    void setUp() {
        controller = new FinancialAnalysisController(getFinancialAnalysisUseCase);
    }

    @Test
    @DisplayName("指定期間の財務分析を取得できる")
    void shouldGetFinancialAnalysisWithPeriod() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        LocalDate comparativeDateFrom = LocalDate.of(2025, 4, 1);
        LocalDate comparativeDateTo = LocalDate.of(2026, 3, 31);
        GetFinancialAnalysisResult result = new GetFinancialAnalysisResult(
                dateFrom, dateTo, comparativeDateFrom, comparativeDateTo,
                List.of(new IndicatorCategory("PROFITABILITY", "収益性",
                        List.of(new FinancialIndicator(
                                "ROE（自己資本利益率）",
                                "%",
                                new BigDecimal("12.34"),
                                new BigDecimal("10.00"),
                                new BigDecimal("2.34"),
                                new BigDecimal("23.40"),
                                "当期純利益 ÷ 自己資本 × 100",
                                new BigDecimal("8.00")
                        ))))
        );
        when(getFinancialAnalysisUseCase.execute(any(GetFinancialAnalysisQuery.class))).thenReturn(result);

        ResponseEntity<GetFinancialAnalysisResult> response = controller.getFinancialAnalysis(
                dateFrom, dateTo, comparativeDateFrom, comparativeDateTo);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetFinancialAnalysisQuery> captor =
                ArgumentCaptor.forClass(GetFinancialAnalysisQuery.class);
        verify(getFinancialAnalysisUseCase).execute(captor.capture());
        assertThat(captor.getValue().dateFrom()).isEqualTo(dateFrom);
        assertThat(captor.getValue().dateTo()).isEqualTo(dateTo);
        assertThat(captor.getValue().comparativeDateFrom()).isEqualTo(comparativeDateFrom);
        assertThat(captor.getValue().comparativeDateTo()).isEqualTo(comparativeDateTo);
    }

    @Test
    @DisplayName("日付未指定で財務分析を取得できる")
    void shouldGetFinancialAnalysisWithoutDates() {
        GetFinancialAnalysisResult result = new GetFinancialAnalysisResult(
                null, null, null, null, List.of()
        );
        when(getFinancialAnalysisUseCase.execute(any(GetFinancialAnalysisQuery.class))).thenReturn(result);

        ResponseEntity<GetFinancialAnalysisResult> response = controller.getFinancialAnalysis(
                null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetFinancialAnalysisQuery> captor =
                ArgumentCaptor.forClass(GetFinancialAnalysisQuery.class);
        verify(getFinancialAnalysisUseCase).execute(captor.capture());
        assertThat(captor.getValue().dateFrom()).isNull();
        assertThat(captor.getValue().dateTo()).isNull();
        assertThat(captor.getValue().comparativeDateFrom()).isNull();
        assertThat(captor.getValue().comparativeDateTo()).isNull();
    }

    @Test
    @DisplayName("アクセス権限は ADMIN と MANAGER のみ")
    void shouldRestrictAccessToAdminAndManager() throws NoSuchMethodException {
        PreAuthorize annotation = FinancialAnalysisController.class
                .getMethod("getFinancialAnalysis", LocalDate.class, LocalDate.class, LocalDate.class, LocalDate.class)
                .getAnnotation(PreAuthorize.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.value()).isEqualTo("hasAnyRole('ADMIN', 'MANAGER')");
    }
}
