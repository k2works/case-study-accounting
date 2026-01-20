package com.example.accounting.infrastructure.persistence.mapper;

import com.example.accounting.infrastructure.persistence.entity.AccountEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * 勘定科目 MyBatis Mapper
 *
 * <p>SQL 定義は mapper/AccountMapper.xml に記述</p>
 */
@Mapper
public interface AccountMapper {

    /**
     * 勘定科目を登録する
     *
     * @param entity 勘定科目エンティティ
     */
    void insert(AccountEntity entity);

    /**
     * 勘定科目を更新する
     *
     * @param entity 勘定科目エンティティ
     * @return 更新件数
     */
    int update(AccountEntity entity);

    /**
     * ID で勘定科目を検索する
     *
     * @param id 勘定科目 ID
     * @return 勘定科目エンティティ
     */
    Optional<AccountEntity> findById(Integer id);

    /**
     * 勘定科目コードで検索する
     *
     * @param code 勘定科目コード
     * @return 勘定科目エンティティ
     */
    Optional<AccountEntity> findByCode(String code);

    /**
     * 全勘定科目を取得する
     *
     * @return 勘定科目エンティティのリスト
     */
    List<AccountEntity> findAll();

    /**
     * 勘定科目種別で検索する
     *
     * @param accountType 勘定科目種別
     * @return 勘定科目エンティティのリスト
     */
    List<AccountEntity> findByType(String accountType);

    /**
     * 勘定科目を削除する
     *
     * @param id 勘定科目 ID
     */
    void deleteById(Integer id);

    /**
     * 勘定科目コードの存在確認
     *
     * @param code 勘定科目コード
     * @return 存在する場合 true
     */
    boolean existsByCode(String code);
}
