package org.example.futoru.repository;

import org.example.futoru.entity.FoodItem;
import org.example.futoru.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * レシピ構成データ(Recipe)へのデータベースアクセスを行うリポジトリ。
 * 料理と食材の結びつき情報の検索に使用する。
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * 指定された「親料理（完成品）」を構成するレシピ情報を取得する。
     * 例: 「カレーライス」を指定して、「白米」「カレールー」などの構成要素リストを取得する際に使用。
     *
     * @param parentFood 親となる料理のマスタデータ
     * @return 紐づいている食材（レシピ構成行）のリスト
     */
    List<Recipe> findByParentFood(FoodItem parentFood);
}