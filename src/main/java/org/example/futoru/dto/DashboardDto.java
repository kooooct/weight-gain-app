package org.example.futoru.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * ダッシュボード画面（トップページ）の表示用データ転送オブジェクト。
 * ユーザーのカロリー摂取状況（目標、現在値、残り）を保持する。
 */
@Data
@AllArgsConstructor
public class DashboardDto {

    /** 1日の目標摂取カロリー (kcal) */
    private int targetCalories;

    /** 当日の合計摂取カロリー (kcal) */
    private int currentCalories;

    /**
     * 目標達成までに必要な残りのカロリー (kcal)。
     * (目標値 - 現在値) で算出される。負の値は目標超過を表す。
     */
    private int remainingCalories;
}