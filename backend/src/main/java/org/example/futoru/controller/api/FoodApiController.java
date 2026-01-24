package org.example.futoru.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.DashboardDto;
import org.example.futoru.dto.FoodUpdateResponse;
import org.example.futoru.entity.MealLog;
import org.example.futoru.service.UserService;
import org.example.futoru.service.FoodService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 食事記録（MealLog）の追加・削除を行うAPIコントローラー。
 * <p>
 * AJAXリクエストを受け付け、食事データの更新を行うとともに、
 * 画面（プログレスバーや履歴リスト）の再描画に必要な最新データを返却する。
 * </p>
 */
@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodApiController {

    private final FoodService foodService;
    private final UserService userService;

    /**
     * マスタデータ（既存の食材・レシピ）から食事記録を追加する。
     *
     * @param foodItemId  選択された食材のID
     * @param amount      摂取量（倍率や個数）
     * @param userDetails 認証済みユーザー情報
     * @return 更新後のダッシュボード情報および食事履歴
     */
    @PostMapping("/add")
    public FoodUpdateResponse addFood(
            @RequestParam Long foodItemId,
            @RequestParam Double amount,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        foodService.recordMealFromMaster(username, foodItemId, amount);
        return createResponse(username);
    }

    /**
     * 手動入力で食事記録を追加する。
     * <p>
     * マスタにない食品や、一時的な記録として名前とカロリーを直接指定する場合に使用する。
     * </p>
     *
     * @param name        食品名
     * @param calories    摂取カロリー
     * @param userDetails 認証済みユーザー情報
     * @return 更新後のダッシュボード情報および食事履歴
     */
    @PostMapping("/manual")
    public FoodUpdateResponse addManual(
            @RequestParam String name,
            @RequestParam int calories,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        foodService.recordManualMeal(username, name, calories);
        return createResponse(username);
    }

    /**
     * 指定された食事記録を削除する。
     *
     * @param id          削除対象のMealLog ID
     * @param userDetails 認証済みユーザー情報
     * @return 更新後のダッシュボード情報および食事履歴
     */
    @PostMapping("/delete/{id}")
    public FoodUpdateResponse deleteFood(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        foodService.deleteMealLog(id, username);
        return createResponse(username);
    }

    /**
     * クライアントへのレスポンスデータを生成するヘルパーメソッド。
     * <p>
     * 最新の食事履歴を取得し、摂取カロリーの合計や目標に対する進捗率を再計算して、
     * 画面更新用DTOに詰め込んで返却する。
     * </p>
     *
     * @param username 対象ユーザー名
     * @return 画面更新用DTO（FoodUpdateResponse）
     */
    private FoodUpdateResponse createResponse(String username) {
        var user = userService.getUserByUsername(username);

        List<MealLog> history = foodService.getTodayMealLogs(username);

        int currentCalories = history.stream().mapToInt(MealLog::getCalories).sum();
        int targetCalories = user.getTargetCalories();

        DashboardDto dashboard = new DashboardDto(
                targetCalories,
                currentCalories,
                targetCalories - currentCalories
        );

        int progress = 0;
        if (targetCalories > 0) {
            progress = (int) ((double) currentCalories / targetCalories * 100);
            progress = Math.min(progress, 100);
        }

        return new FoodUpdateResponse(dashboard, progress, history);
    }
}