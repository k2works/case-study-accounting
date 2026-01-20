package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.UpdateAccountCommand;
import com.example.accounting.application.port.out.UpdateAccountResult;

/**
 * 勘定科目更新ユースケースインターフェース（Input Port）
 *
 * <p>アプリケーション層で定義される Input Port。
 * Web 層（infrastructure.web.controller）から呼び出される。
 * 実装はアプリケーションサービス（application.service）で行う。</p>
 */
public interface UpdateAccountUseCase {

    /**
     * 勘定科目更新を実行する
     *
     * @param command 勘定科目更新コマンド
     * @return 勘定科目更新結果
     */
    UpdateAccountResult execute(UpdateAccountCommand command);
}
