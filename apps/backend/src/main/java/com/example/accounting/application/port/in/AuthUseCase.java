package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.LoginCommand;

/**
 * 認証ユースケースインターフェース（Input Port）
 *
 * <p>アプリケーション層で定義されるInput Port。
 * Web層（infrastructure.web.controller）から呼び出される。
 * 実装はアプリケーションサービス（application.service）で行う。</p>
 */
public interface AuthUseCase {

    /**
     * ログインを実行する
     *
     * @param command ログインコマンド
     * @return ログイン結果
     */
    LoginResult execute(LoginCommand command);
}
