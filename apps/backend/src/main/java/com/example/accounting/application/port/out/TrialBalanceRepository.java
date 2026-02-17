package com.example.accounting.application.port.out;

import com.example.accounting.infrastructure.persistence.entity.TrialBalanceEntity;
import io.vavr.control.Try;
import java.time.LocalDate;
import java.util.List;

/**
 * 試算表リポジトリ（Output Port）
 */
public interface TrialBalanceRepository {
    /**
     * 指定日時点の試算表データを取得します。
     *
     * @param date 集計日
     * @return 取得結果。成功時は試算表データ一覧
     */
    Try<List<TrialBalanceEntity>> findTrialBalance(LocalDate date);
}
