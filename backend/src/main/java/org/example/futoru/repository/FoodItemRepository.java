package org.example.futoru.repository;

import org.example.futoru.entity.FoodItem;
import org.example.futoru.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 食品マスタデータ(FoodItem)へのデータベースアクセスを行うリポジトリ。
 */
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /**
     * 指定されたユーザーが利用可能なすべての食品リストを取得する。
     * <p>
     * 以下の2パターンのデータを結合して取得するカスタムクエリ：
     * 1. ユーザー自身が登録した「My食品」 (f.user = :user)
     * 2. 全ユーザー共通の「システム標準食品」 (f.user IS NULL)
     * </p>
     *
     * @param user 対象のユーザーエンティティ
     * @return 利用可能な食品のリスト
     */
    @Query("SELECT f FROM FoodItem f WHERE f.user = :user OR f.user IS NULL")
    List<FoodItem> findAllAvailable(@Param("user") User user);

    /**
     * 特定のユーザーが独自に作成した食品（My食品）のみを検索する。
     * システム標準の食品は含まれない。
     *
     * @param user 対象のユーザーエンティティ
     * @return そのユーザーが所有する食品リスト
     */
    List<FoodItem> findByUser(User user);
}