package org.example.futoru.repository;

import org.example.futoru.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ユーザー情報(User)へのデータベースアクセスを行うリポジトリ。
 * 認証処理やユーザー管理機能において、DB操作の窓口となるインターフェース。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * ユーザー名（ログインID）を指定してユーザー情報を検索する。
     * 認証時の存在確認やパスワード照合に使用される。
     *
     * @param username 検索対象のユーザー名
     * @return 該当するユーザーのOptional（存在しない場合はEmpty）
     */
    Optional<User> findByUsername(String username);
}