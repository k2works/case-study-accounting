package com.example.accounting.application.port.out;

import com.example.accounting.domain.model.user.User;
import com.example.accounting.domain.model.user.UserId;

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
     * @return 保存されたユーザー
     */
    User save(User user);

    /**
     * ユーザーIDでユーザーを検索する
     *
     * @param id ユーザーID
     * @return ユーザー（存在しない場合は empty）
     */
    Optional<User> findById(UserId id);

    /**
     * ユーザー名でユーザーを検索する
     *
     * @param username ユーザー名
     * @return ユーザー（存在しない場合は empty）
     */
    Optional<User> findByUsername(String username);

    /**
     * メールアドレスでユーザーを検索する
     *
     * @param email メールアドレス
     * @return ユーザー（存在しない場合は empty）
     */
    Optional<User> findByEmail(String email);

    /**
     * すべてのユーザーを取得する
     *
     * @return ユーザーリスト
     */
    List<User> findAll();

    /**
     * ユーザーを削除する
     *
     * @param id ユーザーID
     */
    void deleteById(UserId id);

    /**
     * ユーザー名が存在するかチェックする
     *
     * @param username ユーザー名
     * @return 存在する場合 true
     */
    boolean existsByUsername(String username);

    /**
     * メールアドレスが存在するかチェックする
     *
     * @param email メールアドレス
     * @return 存在する場合 true
     */
    boolean existsByEmail(String email);
}
