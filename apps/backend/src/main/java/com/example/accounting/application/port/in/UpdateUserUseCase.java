package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.command.UpdateUserCommand;
import com.example.accounting.application.port.out.UpdateUserResult;

/**
 * ユーザー更新ユースケース
 */
public interface UpdateUserUseCase {

    /**
     * ユーザー更新を実行する
     *
     * @param command ユーザー更新コマンド
     * @return ユーザー更新結果
     */
    UpdateUserResult execute(UpdateUserCommand command);
}
