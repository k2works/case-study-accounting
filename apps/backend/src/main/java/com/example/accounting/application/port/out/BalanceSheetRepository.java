package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.BalanceSheetEntity;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.List;

/**
 * 貸借対照表リポジトリ（Output Port）
 */
public interface BalanceSheetRepository {
    /**
     * 指定日時点の貸借対照表データを取得します。
     *
     * @param date 集計日
     * @return 取得結果。成功時は貸借対照表データ一覧
     */
    Try<List<BalanceSheetEntity>> findBalanceSheet(LocalDate date);
}
