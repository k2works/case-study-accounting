package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.RegisterUserCommand;
import com.example.accounting.application.port.out.RegisterUserResult;

/**
 * ユーザー登録ユースケースインターフェース（Input Port）
 *
 * <p>アプリケーション層で定義される Input Port。
 * Web 層（infrastructure.web.controller）から呼び出される。
 * 実装はアプリケーションサービス（application.service）で行う。</p>
 */
public interface RegisterUserUseCase {

    /**
     * ユーザー登録を実行する
     *
     * @param command ユーザー登録コマンド
     * @return ユーザー登録結果
     */
    RegisterUserResult execute(RegisterUserCommand command);
}
