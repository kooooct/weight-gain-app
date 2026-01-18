package org.example.futoru.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.futoru.entity.MealLog;

import java.util.List;

/**
 * 食事記録更新時のレスポンスデータを保持するDTOクラス。
 * <p>
 * AJAXによる非同期通信で食事を追加・編集した際に返却される。
 * 画面全体をリロードすることなく、ダッシュボード（プログレスバー）や
 * 食事履歴リストのみを部分更新するために使用する。
 * </p>
 */
@Data
@AllArgsConstructor
public class FoodUpdateResponse {

    /**
     * ダッシュボード表示用データ。
     * カロリーや栄養素の合計値を含み、プログレスバーの再描画に使用する。
     */
    private DashboardDto dashboard;

    /**
     * 現在のカロリー摂取進捗率（%）。
     * プログレスバーの表示更新に使用する。
     */
    private int progress;

    /**
     * 更新後の食事履歴リスト。
     * 画面下部の履歴一覧を再描画するために使用する。
     */
    private List<MealLog> history;
}