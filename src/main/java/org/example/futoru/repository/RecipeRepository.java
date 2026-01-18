package org.example.futoru.repository;

import org.example.futoru.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * レシピ構成データ(Recipe)へのデータベースアクセスを行うリポジトリ。
 * 料理と食材の結びつき情報の検索に使用する。
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}