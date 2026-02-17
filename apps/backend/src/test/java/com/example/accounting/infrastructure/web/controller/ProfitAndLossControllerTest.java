package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetProfitAndLossQuery;
import com.example.accounting.application.port.in.query.GetProfitAndLossUseCase;
import com.example.accounting.application.port.out.GetProfitAndLossResult;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossEntry;
import com.example.accounting.application.port.out.GetProfitAndLossResult.ProfitAndLossSection;
import com.example.accounting.application.service.ProfitAndLossExportService;
import io.vavr.control.Try;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("損益計算書コントローラ")
class ProfitAndLossControllerTest {

    @Mock
    private GetProfitAndLossUseCase getProfitAndLossUseCase;

    @Mock
    private ProfitAndLossExportService exportService;

    private ProfitAndLossController controller;

    @BeforeEach
    void setUp() {
        controller = new ProfitAndLossController(getProfitAndLossUseCase, exportService);
    }

    @Test
    @DisplayName("指定期間の損益計算書を取得できる")
    void shouldGetProfitAndLossWithPeriod() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                dateFrom, dateTo, null, null,
                List.of(new ProfitAndLossSection("REVENUE", "収益の部",
                        List.of(new ProfitAndLossEntry("4000", "売上高", "REVENUE",
                                new BigDecimal("100000"), null)),
                        new BigDecimal("100000"), null)),
                new BigDecimal("100000"),
                BigDecimal.ZERO,
                new BigDecimal("100000")
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);

        ResponseEntity<GetProfitAndLossResult> response = controller.getProfitAndLoss(
                dateFrom, dateTo, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetProfitAndLossQuery> captor = ArgumentCaptor.forClass(GetProfitAndLossQuery.class);
        verify(getProfitAndLossUseCase).execute(captor.capture());
        assertThat(captor.getValue().dateFrom()).isEqualTo(dateFrom);
        assertThat(captor.getValue().dateTo()).isEqualTo(dateTo);
    }

    @Test
    @DisplayName("前期比較期間を指定できる")
    void shouldGetProfitAndLossWithComparativePeriod() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        LocalDate compDateFrom = LocalDate.of(2025, 4, 1);
        LocalDate compDateTo = LocalDate.of(2026, 3, 31);
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                dateFrom, dateTo, compDateFrom, compDateTo,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);

        ResponseEntity<GetProfitAndLossResult> response = controller.getProfitAndLoss(
                dateFrom, dateTo, compDateFrom, compDateTo);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetProfitAndLossQuery> captor = ArgumentCaptor.forClass(GetProfitAndLossQuery.class);
        verify(getProfitAndLossUseCase).execute(captor.capture());
        assertThat(captor.getValue().comparativeDateFrom()).isEqualTo(compDateFrom);
        assertThat(captor.getValue().comparativeDateTo()).isEqualTo(compDateTo);
    }

    @Test
    @DisplayName("日付未指定で全期間の損益計算書を取得できる")
    void shouldGetProfitAndLossWithoutDates() {
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                null, null, null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);

        ResponseEntity<GetProfitAndLossResult> response = controller.getProfitAndLoss(
                null, null, null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetProfitAndLossQuery> captor = ArgumentCaptor.forClass(GetProfitAndLossQuery.class);
        verify(getProfitAndLossUseCase).execute(captor.capture());
        assertThat(captor.getValue().dateFrom()).isNull();
        assertThat(captor.getValue().dateTo()).isNull();
    }

    @Test
    @DisplayName("Excel 形式でエクスポートできる")
    void shouldExportToExcel() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                dateFrom, dateTo, null, null,
                List.of(new ProfitAndLossSection("REVENUE", "収益の部",
                        List.of(new ProfitAndLossEntry("4000", "売上高", "REVENUE",
                                new BigDecimal("100000"), null)),
                        new BigDecimal("100000"), null)),
                new BigDecimal("100000"), BigDecimal.ZERO,
                new BigDecimal("100000")
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);
        byte[] excelBytes = {0x50, 0x4B, 0x03, 0x04};
        when(exportService.exportToExcel(result)).thenReturn(Try.success(excelBytes));

        ResponseEntity<byte[]> response = controller.exportProfitAndLoss(dateFrom, dateTo, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(excelBytes);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("profit-and-loss.xlsx");
    }

    @Test
    @DisplayName("PDF 形式でエクスポートできる")
    void shouldExportToPdf() {
        LocalDate dateFrom = LocalDate.of(2026, 4, 1);
        LocalDate dateTo = LocalDate.of(2027, 3, 31);
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                dateFrom, dateTo, null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);
        byte[] pdfBytes = "%PDF-1.4".getBytes();
        when(exportService.exportToPdf(result)).thenReturn(Try.success(pdfBytes));

        ResponseEntity<byte[]> response = controller.exportProfitAndLoss(dateFrom, dateTo, "pdf");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("profit-and-loss.pdf");
    }

    @Test
    @DisplayName("format 未指定時は Excel をデフォルトとする")
    void shouldDefaultToExcelWhenFormatNotSpecified() {
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                null, null, null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);
        byte[] excelBytes = {0x50, 0x4B, 0x03, 0x04};
        when(exportService.exportToExcel(result)).thenReturn(Try.success(excelBytes));

        ResponseEntity<byte[]> response = controller.exportProfitAndLoss(null, null, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(excelBytes);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("エクスポート失敗時は 500 エラーを返す")
    void shouldReturn500WhenExportFails() {
        GetProfitAndLossResult result = new GetProfitAndLossResult(
                null, null, null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        when(getProfitAndLossUseCase.execute(any(GetProfitAndLossQuery.class))).thenReturn(result);
        when(exportService.exportToExcel(result))
                .thenReturn(Try.failure(new RuntimeException("Export failed")));

        ResponseEntity<byte[]> response = controller.exportProfitAndLoss(null, null, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
