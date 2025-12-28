package org.example.futoru.dto;

/**
 * ユーザーの性別を表す列挙型。
 * 基礎代謝(BMR)の計算式（Mifflin-St Jeor式など）において、性別による補正値を決定するために使用される。
 */
public enum Gender {

    /** 男性 */
    MALE,

    /** 女性 */
    FEMALE;
}