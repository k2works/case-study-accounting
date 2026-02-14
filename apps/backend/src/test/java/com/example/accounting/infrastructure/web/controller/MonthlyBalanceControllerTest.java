package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.GetMonthlyBalanceUseCase;
import com.example.accounting.application.port.in.query.GetMonthlyBalanceQuery;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult;
import com.example.accounting.application.port.out.GetMonthlyBalanceResult.MonthlyBalanceEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("月次残高照会コントローラ")
class MonthlyBalanceControllerTest {

    @Mock
    private GetMonthlyBalanceUseCase getMonthlyBalanceUseCase;

    private MonthlyBalanceController controller;

    @BeforeEach
    void setUp() {
        controller = new MonthlyBalanceController(getMonthlyBalanceUseCase);
    }

    @Test
    @DisplayName("月次残高を取得できる")
    void shouldGetMonthlyBalance() {
        GetMonthlyBalanceResult result = new GetMonthlyBalanceResult(
                "1100", "現金", 2024,
                new BigDecimal("10000"),
                new BigDecimal("5000"),
                new BigDecimal("3000"),
                new BigDecimal("12000"),
                List.of(new MonthlyBalanceEntry(
                        1,
                        new BigDecimal("10000"),
                        new BigDecimal("5000"),
                        new BigDecimal("3000"),
                        new BigDecimal("12000")
                ))
        );
        when(getMonthlyBalanceUseCase.execute(any(GetMonthlyBalanceQuery.class))).thenReturn(result);

        ResponseEntity<GetMonthlyBalanceResult> response = controller.getMonthlyBalance("1100", 2024);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetMonthlyBalanceQuery> captor = ArgumentCaptor.forClass(GetMonthlyBalanceQuery.class);
        verify(getMonthlyBalanceUseCase).execute(captor.capture());
        GetMonthlyBalanceQuery query = captor.getValue();
        assertThat(query.accountCode()).isEqualTo("1100");
        assertThat(query.fiscalPeriod()).isEqualTo(2024);
    }

    @Test
    @DisplayName("fiscalPeriod が null でも取得できる")
    void shouldGetMonthlyBalanceWithoutFiscalPeriod() {
        GetMonthlyBalanceResult result = new GetMonthlyBalanceResult(
                "1100", "現金", null,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                List.of()
        );
        when(getMonthlyBalanceUseCase.execute(any(GetMonthlyBalanceQuery.class))).thenReturn(result);

        ResponseEntity<GetMonthlyBalanceResult> response = controller.getMonthlyBalance("1100", null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetMonthlyBalanceQuery> captor = ArgumentCaptor.forClass(GetMonthlyBalanceQuery.class);
        verify(getMonthlyBalanceUseCase).execute(captor.capture());
        assertThat(captor.getValue().fiscalPeriod()).isNull();
    }
}
