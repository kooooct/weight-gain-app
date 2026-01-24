package org.example.futoru.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * ユーザーの日常的な活動レベル（運動強度）。
 * BMR計算用の補正係数を保持する。
 */
@Getter
@RequiredArgsConstructor
public enum ActivityLevel {

    /**
     * 活動レベル：低
     * デスクワーク中心、または運動習慣がほとんどない。
     */
    LOW(1.2),

    /**
     * 活動レベル：中
     * 立ち仕事が多い、または週に数回の適度な運動。
     */
    MID(1.55),

    /**
     * 活動レベル：高
     * 肉体労働、または毎日激しい運動。
     */
    HIGH(1.725);

    // 補正係数（BmrService計算用）
    private final double multiplier;
}