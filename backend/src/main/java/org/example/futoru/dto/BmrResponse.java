package org.example.futoru.dto;

import lombok.Data;

/**
 * BMR計算結果を格納するレスポンス用DTO。
 */
@Data
public class BmrResponse {
    /** 基礎代謝量 (kcal) */
    private double bmr;

    /** 活動代謝量 / 維持カロリー (kcal) */
    private double tdee;

    /** 増量のための目標摂取カロリー (kcal) */
    private double targetCalories;
}