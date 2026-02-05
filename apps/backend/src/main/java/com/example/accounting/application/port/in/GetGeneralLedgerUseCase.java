package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetGeneralLedgerQuery;
import com.example.accounting.application.port.out.GetGeneralLedgerResult;

public interface GetGeneralLedgerUseCase {
    GetGeneralLedgerResult execute(GetGeneralLedgerQuery query);
}
