package com.example.accounting.application.port.out;

import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult.SubsidiaryLedgerEntry;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface SubsidiaryLedgerRepository {

    List<SubsidiaryLedgerEntry> findPostedLinesByAccountAndSubAccountAndPeriod(
            String accountCode,
            String subAccountCode,
            LocalDate dateFrom,
            LocalDate dateTo,
            int offset,
            int limit
    );

    long countPostedLinesByAccountAndSubAccountAndPeriod(
            String accountCode,
            String subAccountCode,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    BigDecimal calculateBalanceBeforeDateByAccountAndSubAccount(
            String accountCode,
            String subAccountCode,
            LocalDate date
    );
}
