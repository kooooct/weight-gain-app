package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.form.RecipeForm;
import org.example.futoru.entity.FoodItem;
import org.example.futoru.entity.Recipe;
import org.example.futoru.entity.User;
import org.example.futoru.repository.FoodItemRepository;
import org.example.futoru.repository.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * レシピ（複合料理・セットメニュー）に関するビジネスロジックを提供するサービスクラス。
 * <p>
 * 複数の食材や料理を組み合わせた「レシピ」の作成、および構成要素（Recipe）の紐付けを行う。
 * マスタ食材の参照と手入力情報の両方を一括で処理する。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final FoodItemRepository foodItemRepository;
    private final RecipeRepository recipeRepository;
    private final UserService userService;

    /**
     * フォームデータをもとに新しいレシピ（親FoodItem）とその構成要素（Recipe）を保存する。
     * <p>
     * 親となる料理データを作成後、マスタ食材を一括取得して紐付けを行う。
     * 最後に全材料の合計カロリーを算出し、親データを更新する。
     * </p>
     *
     * @param username 作成者のユーザー名
     * @param form     入力データ
     */
    @Transactional
    public void createRecipe(String username, RecipeForm form) {
        User user = userService.getUserByUsername(username);

        FoodItem parentFood = new FoodItem();
        parentFood.setUser(user);
        parentFood.setName(form.getName());
        parentFood.setUnit("人前");
        parentFood.setType(form.getType() != null ? form.getType() : "DISH");
        parentFood.setCalories(0);

        foodItemRepository.save(parentFood);

        List<Long> ids = form.getIngredients().stream()
                .map(RecipeForm.IngredientDto::getFoodItemId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, FoodItem> foodMap = foodItemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(FoodItem::getId, Function.identity()));

        int totalCalories = 0;

        for (RecipeForm.IngredientDto item : form.getIngredients()) {
            Recipe recipe = new Recipe();
            recipe.setParentFood(parentFood);

            if (item.getFoodItemId() != null) {
                FoodItem childFood = foodMap.get(item.getFoodItemId());

                if (childFood == null) {
                    throw new RuntimeException("Ingredient not found ID: " + item.getFoodItemId());
                }

                recipe.setChildFood(childFood);
                recipe.setAmount(item.getAmount() != null ? item.getAmount() : 1.0);

                totalCalories += (int) Math.round(childFood.getCalories() * recipe.getAmount());

            } else {
                recipe.setChildFood(null);
                recipe.setManualName(item.getManualName());
                recipe.setManualCalories(item.getManualCalories());
                recipe.setAmount(1.0);

                totalCalories += (item.getManualCalories() != null ? item.getManualCalories() : 0);
            }

            recipeRepository.save(recipe);
        }

        parentFood.setCalories(totalCalories);
        foodItemRepository.save(parentFood);
    }
}