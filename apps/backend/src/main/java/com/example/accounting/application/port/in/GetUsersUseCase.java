package com.example.accounting.application.port.in;

import com.example.accounting.application.port.in.query.GetUsersQuery;
import com.example.accounting.application.port.in.query.UserSummary;

import java.util.List;

/**
 * ユーザー一覧取得ユースケース
 */
public interface GetUsersUseCase {
    List<UserSummary> execute(GetUsersQuery query);
}
