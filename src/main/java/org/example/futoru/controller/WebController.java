package org.example.futoru.controller;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.DashboardDto;
import org.example.futoru.entity.MealLog;
import org.example.futoru.service.BmrService;
import org.example.futoru.service.FoodService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * アプリケーションの画面遷移を制御するメインコントローラー。
 * トップページ（ダッシュボード）の表示用データ構築を担当する。
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final FoodService foodService;
    private final BmrService bmrService;

    /**
     * ダッシュボード画面（トップページ）を表示する。
     * <p>
     * ログインユーザーの以下の情報を取得し、Viewへ渡す：
     * 1. activePage: ナビゲーションバーの表示制御用フラグ
     * 2. DashboardDto: カロリー目標と現在の摂取状況
     * 3. progress: 目標に対する進捗率（プログレスバー用）
     * 4. history: 今日の食事記録リスト
     * 5. foodList: 記録追加用の食品マスタリスト
     * </p>
     *
     * @param model       画面表示用データモデル
     * @param userDetails Spring Securityによって注入される認証済みユーザー情報
     * @return テンプレート名 ("index")
     */
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        // 1. ナビゲーションバー用設定 (ホームをアクティブ表示)
        model.addAttribute("activePage", "home");

        // 2. 今日の食事履歴を取得
        List<MealLog> todayLogs = foodService.getTodayMealLogs(username);

        // 3. 今日の合計摂取カロリーを計算 (Stream API使用)
        int currentCalories = todayLogs.stream()
                .mapToInt(MealLog::getCalories)
                .sum();

        // 4. 目標カロリーを取得 (BmrServiceを使用)
        int targetCalories = bmrService.calculateTargetCalories(username);

        // 5. 表示用DTOを作成 (目標 - 現在 = 残り)
        DashboardDto dashboard = new DashboardDto(
                targetCalories,
                currentCalories,
                targetCalories - currentCalories
        );
        model.addAttribute("dashboard", dashboard);

        // 6. プログレスバーの進捗率計算
        // カロリーオーバーしても表示が崩れないよう最大100%に制限する
        int progress = 0;
        if (targetCalories > 0) {
            progress = (int) ((double) currentCalories / targetCalories * 100);
            progress = Math.min(progress, 100);
        }
        model.addAttribute("progress", progress);

        // 7. その他のデータをViewへ渡す
        model.addAttribute("history", todayLogs);
        model.addAttribute("foodList", foodService.getAvailableFoods(username));

        return "index";
    }
}