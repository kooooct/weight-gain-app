package org.example.futoru.controller;

import lombok.RequiredArgsConstructor;
import org.example.futoru.service.FoodService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 食事データの登録・削除リクエストを処理するコントローラー。
 */
@Controller
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodController {

    private final FoodService foodService;

    /**
     * マスタデータ（既存の食品）を選択して記録します。
     *
     * @param foodItemId 選択された食品マスタID
     * @param amount     摂取量（1.0 = 1人前/1単位）
     * @param userDetails 認証済みユーザー情報
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/add")
    public String addFood(@RequestParam("foodItemId") Long foodItemId,
                          @RequestParam("amount") Double amount,
                          @AuthenticationPrincipal UserDetails userDetails) {
        foodService.recordMealFromMaster(userDetails.getUsername(), foodItemId, amount);
        return "redirect:/";
    }

    /**
     * 食品名とカロリーを直接手入力して記録します。
     * マスタにない食品を一時的に記録する場合に使用します。
     *
     * @param name     食品名
     * @param calories カロリー
     * @param userDetails 認証済みユーザー情報
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/manual")
    public String addManualFood(@RequestParam("name") String name,
                                @RequestParam("calories") int calories,
                                @AuthenticationPrincipal UserDetails userDetails) {
        foodService.recordManualMeal(userDetails.getUsername(), name, calories);
        return "redirect:/";
    }

    /**
     * 指定された食事記録を削除します。
     *
     * @param id 削除対象のMealLog ID
     * @param userDetails 認証済みユーザー情報
     * @return ダッシュボードへのリダイレクト
     */
    @PostMapping("/delete/{id}")
    public String deleteFood(@PathVariable("id") Long id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        foodService.deleteMealLog(id, userDetails.getUsername());
        return "redirect:/";
    }
}