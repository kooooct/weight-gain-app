package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ユーザーの食事記録を管理するエンティティ。
 * <p>
 * マスタデータ(FoodItem)への参照を持つだけでなく、
 * 記録時点での食品名やカロリーを「スナップショット」として保持する設計。
 * これにより、将来マスタデータが変更・削除されても、過去の食事履歴は保護される。
 * </p>
 */
@Entity
@Table(name = "meal_logs")
@Data
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** この記録を作成したユーザー */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 参照元の食品マスタ。
     * マスタリストから選択した場合はIDが入る。
     * 手入力で記録した場合や、参照元が削除された場合はNULLとなる可能性がある。
     */
    @ManyToOne
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    /**
     * 記録時点での食品名 (スナップショット)。
     * マスタデータ側の名前が変更されても、ここには記録時の名前が残る。
     */
    private String name;

    /**
     * 記録時点での合計摂取カロリー (スナップショット)。
     * 計算式: マスタの基準カロリー × 摂取量(amount)
     */
    private Integer calories;

    /**
     * 摂取量。
     * マスタデータの単位（例: 1個, 100g）に対する倍率。
     * (例: 1.5 = 1.5人前 / 1.5個)
     */
    private Double amount;

    /** 食事をした日時 */
    @Column(name = "eaten_at", nullable = false)
    private LocalDateTime eatenAt;
}