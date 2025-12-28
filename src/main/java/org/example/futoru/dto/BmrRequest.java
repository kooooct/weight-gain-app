package org.example.futoru.dto;

import lombok.Data;

/**
 * BMR（基礎代謝）計算リクエスト用のデータ転送オブジェクト（DTO）。
 * クライアントから送信される身体データを受け取るために使用される。
 */
@Data
public class BmrRequest {

    /** 身長 (cm) */
    private Double height;

    /** 体重 (kg) */
    private Double weight;

    /** 年齢 */
    private Integer age;

    /** 性別 */
    private Gender gender;

    /** 活動レベル */
    private ActivityLevel activityLevel;
}