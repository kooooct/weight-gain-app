package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 料理のレシピ構成（親子関係）を管理するエンティティ。
 * <p>
 * 1つの料理（Parent）が、どの食材（Child）をどれだけの量（Amount）含んでいるかを定義する中間テーブル。
 * 例: 「カレーライス(Parent)」は「白米(Child) 1人前」と「カレールー(Child) 1人前」から構成される。
 * </p>
 */
@Entity
@Table(name = "recipes")
@Data
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 親となる料理（完成品）。
     * 例: カレーライス
     */
    @ManyToOne
    @JoinColumn(name = "parent_food_id", nullable = false)
    private FoodItem parentFood;

    /**
     * 構成要素となる食材（材料）。
     * 例: じゃがいも、豚肉
     */
    @ManyToOne
    @JoinColumn(name = "child_food_id", nullable = false)
    private FoodItem childFood;

    /**
     * 構成量。
     * 食材マスタの単位に対する倍率。
     */
    private Double amount;
}