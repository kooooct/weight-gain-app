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
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        // 1. 親となる FoodItem (例: "朝食セット") を作成
        FoodItem parentFood = new FoodItem();
        parentFood.setUser(user);
        parentFood.setName(form.getName());
        parentFood.setUnit("人前"); // レシピの単位は暫定で「人前」固定
        parentFood.setType("DISH"); // 区分は「料理(DISH)」とする
        parentFood.setCalories(0);  // 後で計算して更新するため、初期値は0

        // IDを発行させるため、先に親データを保存する
        foodItemRepository.save(parentFood);

        int totalCalories = 0;

        // 2. 材料リストをループして、Recipeテーブル（中間テーブル）に保存していく
        for (RecipeForm.IngredientDto item : form.getIngredients()) {

            // 使用する食材（Child）を検索
            FoodItem childFood = foodItemRepository.findById(item.getFoodItemId())
                    .orElseThrow(() -> new RuntimeException("Ingredient not found ID: " + item.getFoodItemId()));

            // Recipeエンティティ（親と子の紐付け情報）を作成
            Recipe recipe = new Recipe();
            recipe.setParentFood(parentFood); // 親：今作成しているレシピ
            recipe.setChildFood(childFood);   // 子：材料となる食材
            recipe.setAmount(item.getAmount());

            recipeRepository.save(recipe);

            // カロリー計算: (材料のカロリー × 使用量) を加算
            // ※必要に応じてdoubleからintへのキャストや四捨五入の精度を調整可能
            totalCalories += (int) (childFood.getCalories() * item.getAmount());
        }

        // 3. 合計カロリーが確定したので、親のFoodItemを更新して保存
        parentFood.setCalories(totalCalories);
        foodItemRepository.save(parentFood);
    }
}