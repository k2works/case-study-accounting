package com.example.accounting.infrastructure.web.controller;

import com.example.accounting.application.port.in.query.GetBalanceSheetQuery;
import com.example.accounting.application.port.in.query.GetBalanceSheetUseCase;
import com.example.accounting.application.port.out.GetBalanceSheetResult;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetEntry;
import com.example.accounting.application.port.out.GetBalanceSheetResult.BalanceSheetSection;
import com.example.accounting.application.service.BalanceSheetExportService;
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
@DisplayName("貸借対照表コントローラ")
class BalanceSheetControllerTest {

    @Mock
    private GetBalanceSheetUseCase getBalanceSheetUseCase;

    @Mock
    private BalanceSheetExportService exportService;

    private BalanceSheetController controller;

    @BeforeEach
    void setUp() {
        controller = new BalanceSheetController(getBalanceSheetUseCase, exportService);
    }

    @Test
    @DisplayName("指定日の貸借対照表を取得できる")
    void shouldGetBalanceSheetWithDate() {
        LocalDate date = LocalDate.of(2026, 3, 31);
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                date, null,
                List.of(new BalanceSheetSection("ASSET", "資産の部",
                        List.of(new BalanceSheetEntry("100", "現金", "ASSET",
                                new BigDecimal("100000"), null)),
                        new BigDecimal("100000"), null)),
                new BigDecimal("100000"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                false,
                new BigDecimal("100000")
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);

        ResponseEntity<GetBalanceSheetResult> response = controller.getBalanceSheet(date, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(result);

        ArgumentCaptor<GetBalanceSheetQuery> captor = ArgumentCaptor.forClass(GetBalanceSheetQuery.class);
        verify(getBalanceSheetUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isEqualTo(date);
        assertThat(captor.getValue().comparativeDate()).isNull();
    }

    @Test
    @DisplayName("date と comparativeDate を両方指定できる")
    void shouldGetBalanceSheetWithComparativeDate() {
        LocalDate date = LocalDate.of(2026, 3, 31);
        LocalDate comparativeDate = LocalDate.of(2025, 3, 31);
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                date, comparativeDate,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);

        ResponseEntity<GetBalanceSheetResult> response = controller.getBalanceSheet(date, comparativeDate);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetBalanceSheetQuery> captor = ArgumentCaptor.forClass(GetBalanceSheetQuery.class);
        verify(getBalanceSheetUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isEqualTo(date);
        assertThat(captor.getValue().comparativeDate()).isEqualTo(comparativeDate);
    }

    @Test
    @DisplayName("date が null で全期間の貸借対照表を取得できる")
    void shouldGetBalanceSheetWithoutDate() {
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);

        ResponseEntity<GetBalanceSheetResult> response = controller.getBalanceSheet(null, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<GetBalanceSheetQuery> captor = ArgumentCaptor.forClass(GetBalanceSheetQuery.class);
        verify(getBalanceSheetUseCase).execute(captor.capture());
        assertThat(captor.getValue().date()).isNull();
        assertThat(captor.getValue().comparativeDate()).isNull();
    }

    @Test
    @DisplayName("Excel 形式でエクスポートできる")
    void shouldExportToExcel() {
        LocalDate date = LocalDate.of(2026, 3, 31);
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                date, null,
                List.of(new BalanceSheetSection("ASSET", "資産の部",
                        List.of(new BalanceSheetEntry("100", "現金", "ASSET",
                                new BigDecimal("100000"), null)),
                        new BigDecimal("100000"), null)),
                new BigDecimal("100000"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);
        byte[] excelBytes = {0x50, 0x4B, 0x03, 0x04};
        when(exportService.exportToExcel(result)).thenReturn(Try.success(excelBytes));

        ResponseEntity<byte[]> response = controller.exportBalanceSheet(date, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(excelBytes);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("balance-sheet.xlsx");
    }

    @Test
    @DisplayName("PDF 形式でエクスポートできる")
    void shouldExportToPdf() {
        LocalDate date = LocalDate.of(2026, 3, 31);
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                date, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);
        byte[] pdfBytes = "%PDF-1.4".getBytes();
        when(exportService.exportToPdf(result)).thenReturn(Try.success(pdfBytes));

        ResponseEntity<byte[]> response = controller.exportBalanceSheet(date, "pdf");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(pdfBytes);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("balance-sheet.pdf");
    }

    @Test
    @DisplayName("format 未指定時は Excel をデフォルトとする")
    void shouldDefaultToExcelWhenFormatNotSpecified() {
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);
        byte[] excelBytes = {0x50, 0x4B, 0x03, 0x04};
        when(exportService.exportToExcel(result)).thenReturn(Try.success(excelBytes));

        ResponseEntity<byte[]> response = controller.exportBalanceSheet(null, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(excelBytes);
        assertThat(response.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("エクスポート失敗時は 500 エラーを返す")
    void shouldReturn500WhenExportFails() {
        GetBalanceSheetResult result = new GetBalanceSheetResult(
                null, null,
                List.of(), BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, true, BigDecimal.ZERO
        );
        when(getBalanceSheetUseCase.execute(any(GetBalanceSheetQuery.class))).thenReturn(result);
        when(exportService.exportToExcel(result)).thenReturn(Try.failure(new RuntimeException("Export failed")));

        ResponseEntity<byte[]> response = controller.exportBalanceSheet(null, "excel");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
