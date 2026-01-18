package org.example.futoru.controller;

import lombok.RequiredArgsConstructor;
import org.example.futoru.form.RecipeForm;
import org.example.futoru.service.FoodService;
import org.example.futoru.service.RecipeService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * レシピ（セット料理）の管理機能を提供するコントローラークラス。
 * <p>
 * レシピ作成画面の表示や、JavaScript(AJAX)からの保存リクエストを処理する。
 * </p>
 */
@Controller
@RequestMapping("/recipe")
@RequiredArgsConstructor
public class RecipeController {

    private final FoodService foodService;
    private final RecipeService recipeService;

    /**
     * レシピ作成画面を表示する。
     * <p>
     * 画面左側のリスト（ドラッグ＆ドロップ用）に表示するため、
     * 利用可能な全食材データを取得してModelに格納する。
     * </p>
     *
     * @param model       画面にデータを渡すためのModel
     * @param userDetails ログイン中のユーザー情報
     * @return テンプレートパス ("recipe/create")
     */
    @GetMapping("/create")
    public String showCreatePage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // 左側のリストに表示するために、全食材を取得して渡す
        model.addAttribute("foodList", foodService.getAvailableFoods(userDetails.getUsername()));

        // アクティブなタブ指定（ナビゲーションバーの強調表示用）
        model.addAttribute("activePage", "recipe");

        return "recipe/create";
    }

    /**
     * 新規レシピの保存処理を行う（AJAX対応）。
     * <p>
     * 画面側から送信されたJSONデータを {@link RecipeForm} に変換して受け取り、
     * サービス層を介して保存処理を行う。
     * {@code @ResponseBody} が付いているため、HTMLではなくJSONデータをレスポンスとして返す。
     * </p>
     *
     * @param form        画面からの入力データ（JSONから変換）
     * @param userDetails ログイン中のユーザー情報
     * @return クライアントへのレスポンス（成功ステータスを含むMap）
     */
    @PostMapping("/create")
    @ResponseBody
    public Map<String, String> createRecipe(
            @RequestBody RecipeForm form,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        recipeService.createRecipe(userDetails.getUsername(), form);

        // クライアント側（JS）に成功したことを伝える
        return Map.of("status", "success");
    }
}