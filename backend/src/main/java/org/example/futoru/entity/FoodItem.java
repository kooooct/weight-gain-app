package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 食品マスタデータを管理するエンティティ。
 * ユーザーが食事記録をつける際に参照する「食品の辞書」となるデータ。
 */
@Entity
@Table(name = "food_items")
@Data
public class FoodItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * この食品を作成したユーザー。
     * nullの場合、全ユーザーが利用可能な「システム標準食品」として扱われる。
     * 値がある場合、そのユーザー専用の「My食品」となる。
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /** 食品名 (例: 白米, 鶏胸肉) */
    private String name;

    /** 1単位あたりの基準カロリー (kcal) */
    private Integer calories;

    /** 単位 (例: "個", "皿", "g") */
    private String unit;

    /**
     * INGREDIENT: 食材（肉、野菜、既製品）
     * DISH: 料理（レシピで作られたもの。カレー、ハンバーグ）
     * MEAL_SET: 定食・セット（料理の組み合わせ。Aセット）
     */
    private String type;
}