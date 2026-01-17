package org.example.futoru.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.DashboardDto;
import org.example.futoru.dto.FoodUpdateResponse;
import org.example.futoru.entity.MealLog;
import org.example.futoru.service.BmrService;
import org.example.futoru.service.FoodService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodApiController {

    private final FoodService foodService;
    private final BmrService bmrService;

    // 1. マスタから追加
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

    // 2. 手動入力
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

    // 3. 削除
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
     * 画面更新に必要な全データをまとめて作るヘルパーメソッド
     */
    private FoodUpdateResponse createResponse(String username) {
        // 1. 最新の履歴を取得
        List<MealLog> history = foodService.getTodayMealLogs(username);

        // 2. カロリー計算
        int currentCalories = history.stream().mapToInt(MealLog::getCalories).sum();
        int targetCalories = bmrService.calculateTargetCalories(username);

        DashboardDto dashboard = new DashboardDto(
                targetCalories,
                currentCalories,
                targetCalories - currentCalories
        );

        // 3. 進捗率計算
        int progress = 0;
        if (targetCalories > 0) {
            progress = (int) ((double) currentCalories / targetCalories * 100);
            progress = Math.min(progress, 100);
        }

        return new FoodUpdateResponse(dashboard, progress, history);
    }
}