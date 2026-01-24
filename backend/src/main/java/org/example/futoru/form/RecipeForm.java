package org.example.futoru.form;

import lombok.Data;
import java.util.List;

/**
 * レシピ（セット）作成画面からの入力データを受け取るフォームクラス。
 * <p>
 * 料理（DISH）または定食（MEAL_SET）の作成時に使用される。
 * マスタデータ（既存の食材）の選択と、ユーザーによる直接入力の両方の材料情報を保持できる。
 * </p>
 */
@Data
public class RecipeForm {

    /** レシピ名（例：「自家製ハンバーグ」「ランチセットA」） */
    private String name;

    /**
     * 作成するアイテムの区分。
     * <p>
     * "DISH"（料理単品）または "MEAL_SET"（複数の料理を組み合わせたセット）のいずれかが格納される。
     * </p>
     */
    private String type;

    /** 構成する材料（または料理）のリスト */
    private List<IngredientDto> ingredients;

    /**
     * 材料1つ分のデータを保持する内部クラス。
     * <p>
     * {@code foodItemId} が存在する（非null）場合はマスタデータを参照し、
     * 存在しない（null）場合は {@code manualName} および {@code manualCalories} を使用する。
     * </p>
     */
    @Data
    public static class IngredientDto {

        /** マスタデータの食材ID（手入力の場合は null） */
        private Long foodItemId;

        /** 手入力時の材料名（マスタを使用しない場合に使用） */
        private String manualName;

        /** 手入力時のカロリー（マスタを使用しない場合に使用） */
        private Integer manualCalories;

        /** 使用量（1.0個、2.0倍など） */
        private Double amount;
    }
}