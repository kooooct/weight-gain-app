package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.form.RecipeForm;
import org.example.futoru.entity.FoodItem;
import org.example.futoru.entity.Recipe;
import org.example.futoru.entity.User;
import org.example.futoru.repository.FoodItemRepository;
import org.example.futoru.repository.RecipeRepository;
import org.example.futoru.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * レシピ（複合料理）に関するビジネスロジックを提供するサービスクラス。
 * <p>
 * 複数の食材を組み合わせた「レシピ」の作成処理や、
 * それに伴うカロリーの自動計算、データベースへの保存処理を担う。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final FoodItemRepository foodItemRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    /**
     * フォームから受け取ったデータをもとに、新しいレシピを作成・保存する。
     * <p>
     * 以下の手順で処理を行う：
     * 1. 親となるFoodItem（料理自体）を仮保存しIDを発行する。
     * 2. 材料リストをループし、中間テーブル（Recipe）に紐付け情報を保存する。
     * 3. 各材料のカロリーを加算して総カロリーを計算し、親FoodItemを更新する。
     * </p>
     *
     * @param username 作成者のユーザー名
     * @param form     画面からの入力データ（レシピ名、材料リスト）
     */
    @Transactional
    public void createRecipe(String username, RecipeForm form) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. 親となる FoodItem を作成
        FoodItem parentFood = new FoodItem();
        parentFood.setUser(user);
        parentFood.setName(form.getName());
        parentFood.setUnit("人前");

        // ★重要: フォームで選ばれたタイプ("DISH" or "MEAL_SET")を保存
        // 指定がなければデフォルトで "DISH" にする安全策
        parentFood.setType(form.getType() != null ? form.getType() : "DISH");

        parentFood.setCalories(0);
        foodItemRepository.save(parentFood);

        int totalCalories = 0;

        // 2. 材料リストを保存
        List<Long> ids = form.getIngredients().stream()
                .map(RecipeForm.IngredientDto::getFoodItemId)
                .filter(Objects::nonNull)
                .toList();

// 一括取得（1回のSQLで済む）
        List<FoodItem> foods = foodItemRepository.findAllById(ids);
        Map<Long, FoodItem> foodMap = foods.stream()
                .collect(Collectors.toMap(FoodItem::getId, f -> f));

// ループ処理
        for (RecipeForm.IngredientDto item : form.getIngredients()) {
            Recipe recipe = new Recipe();
            recipe.setParentFood(parentFood);

            // 分岐: マスタ選択か、手入力か？
            if (item.getFoodItemId() != null) {
                // === A. マスタ選択の場合 ===
                FoodItem childFood = foodItemRepository.findById(item.getFoodItemId())
                        .orElseThrow(() -> new RuntimeException("Ingredient not found"));

                recipe.setChildFood(childFood);
                recipe.setAmount(item.getAmount() != null ? item.getAmount() : 1.0);

                // カロリー計算: マスタ × 量
                totalCalories += (int) Math.round(childFood.getCalories() * recipe.getAmount());

            } else {
                // === B. 手入力の場合 ===
                recipe.setChildFood(null);
                recipe.setManualName(item.getManualName());
                recipe.setManualCalories(item.getManualCalories());
                recipe.setAmount(1.0); // 手入力は1人前固定とする

                // カロリー計算: 手入力値をそのまま足す
                totalCalories += item.getManualCalories();
            }

            recipeRepository.save(recipe);
        }

        // 3. 合計カロリー更新
        parentFood.setCalories(totalCalories);
        foodItemRepository.save(parentFood);
    }
}