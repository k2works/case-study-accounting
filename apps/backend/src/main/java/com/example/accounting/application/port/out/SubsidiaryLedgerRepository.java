package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;
import io.vavr.control.Try;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubsidiaryLedgerRepository {

    /**
     * 指定条件に一致する補助元帳明細を取得します。
     *
     * @return 取得結果。成功時は補助元帳明細一覧
     */
    Try<List<SubsidiaryLedgerEntry>> findPostedLinesByAccountAndSubAccountAndPeriod(
            String accountCode,
            String subAccountCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    );

    /**
     * 指定条件に一致する補助元帳明細件数を取得します。
     *
     * @return 取得結果。成功時は明細件数
     */
    Try<Long> countPostedLinesByAccountAndSubAccountAndPeriod(
            String accountCode,
            String subAccountCode,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    /**
     * 指定日より前の残高を取得します。
     *
     * @return 取得結果。成功時は残高
     */
    Try<BigDecimal> calculateBalanceBeforeDateByAccountAndSubAccount(
            String accountCode,
            String subAccountCode,
            LocalDate date
    );
}
