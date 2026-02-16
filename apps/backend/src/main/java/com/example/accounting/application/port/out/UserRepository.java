package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;
import io.vavr.control.Try;

import java.util.List;
import java.util.Optional;

/**
 * ユーザーリポジトリインターフェース（Output Port）
 *
 * <p>アプリケーション層で定義されるOutput Port。
 * 実装はインフラストラクチャ層（infrastructure.persistence.repository）で行う。</p>
 */
public interface UserRepository {

    /**
     * ユーザーを保存する
     *
     * @param user ユーザー
     * @return 保存されたユーザーを含む Try
     */
    Try<User> save(User user);

    /**
     * ユーザーIDでユーザーを検索する
     *
     * @param id ユーザーID
     * @return ユーザー（存在しない場合は empty）を含む Try
     */
    Try<Optional<User>> findById(UserId id);

    /**
     * ユーザー名でユーザーを検索する
     *
     * @param username ユーザー名
     * @return ユーザー（存在しない場合は empty）を含む Try
     */
    Try<Optional<User>> findByUsername(String username);

    /**
     * メールアドレスでユーザーを検索する
     *
     * @param email メールアドレス
     * @return ユーザー（存在しない場合は empty）を含む Try
     */
    Try<Optional<User>> findByEmail(String email);

    /**
     * すべてのユーザーを取得する
     *
     * @return ユーザーリストを含む Try
     */
    Try<List<User>> findAll();

    /**
     * 条件でユーザーを検索する
     *
     * @param role    ロール（null の場合は全件）
     * @param keyword 検索キーワード（null の場合は全件）
     * @return ユーザーリストを含む Try
     */
    Try<List<User>> search(String role, String keyword);

    /**
     * ユーザーを削除する
     *
     * @param id ユーザーID
     */
    Try<Void> deleteById(UserId id);

    /**
     * ユーザー名が存在するかチェックする
     *
     * @param username ユーザー名
     * @return 存在する場合 true を含む Try
     */
    Try<Boolean> existsByUsername(String username);

    /**
     * メールアドレスが存在するかチェックする
     *
     * @param email メールアドレス
     * @return 存在する場合 true を含む Try
     */
    Try<Boolean> existsByEmail(String email);
}
