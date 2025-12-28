package org.example.futoru.controller;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.DashboardDto;
import org.example.futoru.entity.FoodItem;
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
     * 1. DashboardDto: カロリー目標と現在の摂取状況
     * 2. history: 今日の食事記録リスト
     * 3. foodList: 記録追加用の食品マスタリスト
     * </p>
     *
     * @param model       画面表示用データモデル
     * @param userDetails Spring Securityによって注入される認証済みユーザー情報
     * @return テンプレート名 ("index")
     */
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        // 1. 今日の食事履歴を取得
        List<MealLog> todayLogs = foodService.getTodayMealLogs(username);

        // 2. 今日の合計摂取カロリーを計算 (Stream API使用)
        int currentCalories = todayLogs.stream()
                .mapToInt(MealLog::getCalories)
                .sum();

        // 3. 目標カロリーを取得
        // プロフィール未設定時はService側でデフォルト値が返されるため、例外処理は不要
        int targetCalories = bmrService.calculateTargetCalories(username);

        // 4. 表示用DTOを作成 (目標 - 現在 = 残り)
        DashboardDto dashboard = new DashboardDto(
                targetCalories,
                currentCalories,
                targetCalories - currentCalories
        );

        // 5. 食事記録プルダウン用の食品リストを取得
        List<FoodItem> foodList = foodService.getAvailableFoods(username);

        // Viewへデータを渡す
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("history", todayLogs); // View側では ${history} で参照
        model.addAttribute("foodList", foodList); // View側では ${foodList} で参照

        return "index";
    }
}