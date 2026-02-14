package com.example.accounting.application.port.in.query;

import com.example.accounting.application.port.out.GetBalanceSheetResult;

/**
 * 貸借対照表照会ユースケース
 */
public interface GetBalanceSheetUseCase {
    GetBalanceSheetResult execute(GetBalanceSheetQuery query);
}
