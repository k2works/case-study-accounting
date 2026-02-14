package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetSubsidiaryLedgerQuery;
import com.example.accounting.application.port.out.GetSubsidiaryLedgerResult;

public interface GetSubsidiaryLedgerUseCase {
    GetSubsidiaryLedgerResult execute(GetSubsidiaryLedgerQuery query);
}
