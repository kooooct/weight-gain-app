package org.example.futoru.controller;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.DashboardDto;
import org.example.futoru.entity.MealLog;
import org.example.futoru.service.FoodService;
import org.example.futoru.service.UserService;
import org.example.futoru.service.WeightLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

/**
 * アプリケーションのメイン画面（ダッシュボード）や共通の画面遷移を制御するコントローラークラス。
 * <p>
 * 複数のサービス（食事、体重、ユーザー情報）を集約し、
 * トップページに表示するための統計データやグラフデータを構築する役割を担う。
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final FoodService foodService;
    private final UserService userService;
    private final WeightLogService weightLogService;

    /**
     * ダッシュボード画面（トップページ）を表示する。
     * <p>
     * 画面表示に必要な全てのデータ（食事履歴、カロリー進捗、体重グラフなど）を一括して取得する。
     * また、ユーザーがプロフィール（身長・体重など）を未設定の場合は、初期設定画面へ強制リダイレクトする制御もここで行う。
     * </p>
     *
     * @param model       画面表示用データモデル
     * @param userDetails Spring Securityによって注入される認証済みユーザー情報
     * @return テンプレート名 ("index") または リダイレクトパス
     */
    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        if (!userService.isProfileCompleted(username)) {
            return "redirect:/profile/init";
        }

        var user = userService.getUserByUsername(username);

        model.addAttribute("activePage", "home");

        List<MealLog> todayLogs = foodService.getTodayMealLogs(username);

        int currentCalories = todayLogs.stream()
                .mapToInt(MealLog::getCalories)
                .sum();

        int targetCalories = user.getTargetCalories();

        DashboardDto dashboard = new DashboardDto(
                targetCalories,
                currentCalories,
                targetCalories - currentCalories
        );
        model.addAttribute("dashboard", dashboard);

        // プログレスバーの進捗率計算（最大100%に制限）
        int progress = 0;
        if (targetCalories > 0) {
            progress = (int) ((double) currentCalories / targetCalories * 100);
            progress = Math.min(progress, 100);
        }
        model.addAttribute("progress", progress);

        model.addAttribute("history", todayLogs);
        model.addAttribute("foodList", foodService.getAvailableFoods(username));

        // Chart.js 用データ
        model.addAttribute("weightDates", weightLogService.getGraphLabels(username));
        model.addAttribute("weightValues", weightLogService.getGraphValues(username));

        return "index";
    }

    /**
     * 工事中画面を表示する。
     * <p>
     * 未実装の機能へアクセスされた場合に表示するプレースホルダー画面。
     * </p>
     *
     * @param model 画面表示用データモデル
     * @return テンプレート名 ("under-construction")
     */
    @GetMapping("/under-construction")
    public String underConstruction(Model model) {
        model.addAttribute("activePage", "none");
        return "under-construction";
    }

    /**
     * 体重記録を追加する。
     * <p>
     * ダッシュボード上のモーダルウィンドウから送信された体重データを受け取り保存する。
     * 処理完了後はトップページへリダイレクトし、グラフを更新させる。
     * </p>
     *
     * @param datestr     日付文字列（HTML5 date input形式: "yyyy-MM-dd"）
     * @param weight      体重 (kg)
     * @param userDetails 認証済みユーザー情報
     * @return トップページへのリダイレクトパス
     */
    @PostMapping("/weight/add")
    public String addWeight(
            @RequestParam("date") String datestr,
            @RequestParam("weight") Double weight,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LocalDate date = LocalDate.parse(datestr);

        weightLogService.saveWeightLog(userDetails.getUsername(), date, weight);
        return "redirect:/";
    }
}