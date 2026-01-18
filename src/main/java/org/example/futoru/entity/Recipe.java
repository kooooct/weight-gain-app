package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * レシピの構成要素（材料）を管理するエンティティクラス。
 * <p>
 * 親となる料理（FoodItem）と、それに含まれる材料（子FoodItem）の中間テーブルとしての役割を持つ。
 * データベースに登録済みの食材（マスタ）を参照する場合と、
 * ユーザーが手入力した情報（manualName/manualCalories）を使用する場合の、
 * 両方に対応できる構造となっている。
 * </p>
 */
@Entity
@Table(name = "recipes")
@Data
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 親となる料理（完成品）。例: 「カレーセット」 */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_food_id", nullable = false)
    private FoodItem parentFood;

    /**
     * 材料となる食材（マスタデータ）。例: 「カレーライス」
     * <p>
     * マスタデータを使用する場合はセットされる。
     * 手入力の場合は {@code null} となる。
     * </p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_food_id", nullable = true)
    private FoodItem childFood;

    /**
     * 手入力用の材料名。
     * <p>
     * マスタデータを使用しない（childFoodがnullの）場合に使用する。
     * </p>
     */
    @Column(name = "manual_name")
    private String manualName;

    /**
     * 手入力用のカロリー。
     * <p>
     * マスタデータを使用しない（childFoodがnullの）場合に使用する。
     * </p>
     */
    @Column(name = "manual_calories")
    private Integer manualCalories;

    /**
     * 使用量（倍率）。
     * <p>
     * マスタがある場合はその基準量に対する倍率（例: 1.5倍）。
     * 手入力の場合は計算済みカロリーを入力するため、基本的には使用しないか 1.0 固定となる想定。
     * </p>
     */
    private Double amount;

    /**
     * 表示用の名前を取得する便利メソッド。
     * <p>
     * マスタデータ（childFood）がある場合はその名前を、
     * ない場合は手入力された名前（manualName）を返す。
     * </p>
     *
     * @return 表示すべき材料名
     */
    public String getDisplayName() {
        if (childFood != null) {
            return childFood.getName();
        }
        return manualName;
    }

    /**
     * 計算用のカロリーを取得する便利メソッド。
     * <p>
     * マスタデータがある場合は「マスタのカロリー × 量」を計算して返し、
     * 手入力の場合は入力されたカロリー値をそのまま返す。
     * </p>
     *
     * @return この材料の合計カロリー
     */
    public int getCalculatedCalories() {
        if (childFood != null) {
            // マスタのカロリー × 倍率
            return (int) Math.round(childFood.getCalories() * amount);
        }
        // 手入力カロリー（nullの場合は0として扱う）
        return manualCalories != null ? manualCalories : 0;
    }
}