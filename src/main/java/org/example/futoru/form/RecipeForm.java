package org.example.futoru.form;

import lombok.Data;
import java.util.List;

/**
 * レシピ（セット）作成画面から送られてくるデータを受け取るクラス。
 * <p>
 * 複数の食材を組み合わせた「レシピ」を新規作成または編集する際に使用する。
 * 画面側から送信されるJSONデータ（レシピ名と材料リスト）にマッピングされる。
 * </p>
 */
@Data
public class RecipeForm {

    /** レシピ名（例：「朝食セットA」） */
    private String name;

    /** 材料のリスト（使用する食材とその量のペア） */
    private List<IngredientDto> ingredients;

    /**
     * レシピに含まれる材料1つ分のデータを表す内部クラス。
     * <p>
     * どの食材（ID）をどれだけの量（amount）使用するかを保持する。
     * </p>
     */
    @Data
    public static class IngredientDto {

        /** 食材のID（FoodItemエンティティのID） */
        private Long foodItemId;

        /** 使用量（1.0個、100gなど） */
        private Double amount;
    }
}