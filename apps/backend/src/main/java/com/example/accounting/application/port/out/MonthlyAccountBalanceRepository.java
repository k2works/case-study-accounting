package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.MonthlyAccountBalanceEntity;
import io.vavr.control.Try;
import java.util.List;

public interface MonthlyAccountBalanceRepository {
    /**
     * 勘定科目と会計期間に紐づく月次残高を取得します。
     *
     * @param accountCode 勘定科目コード
     * @param fiscalPeriod 会計期間
     * @return 取得結果。成功時は月次残高一覧
     */
    Try<List<MonthlyAccountBalanceEntity>> findByAccountCodeAndFiscalPeriod(
            String accountCode, Integer fiscalPeriod);
}
