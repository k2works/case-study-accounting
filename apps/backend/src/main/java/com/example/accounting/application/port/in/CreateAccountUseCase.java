package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.CreateAccountCommand;
import com.example.accounting.application.port.out.CreateAccountResult;

/**
 * 勘定科目登録ユースケースインターフェース（Input Port）
 *
 * <p>アプリケーション層で定義される Input Port。
 * Web 層（infrastructure.web.controller）から呼び出される。
 * 実装はアプリケーションサービス（application.service）で行う。</p>
 */
public interface CreateAccountUseCase {

    /**
     * 勘定科目登録を実行する
     *
     * @param command 勘定科目登録コマンド
     * @return 勘定科目登録結果
     */
    CreateAccountResult execute(CreateAccountCommand command);
}
