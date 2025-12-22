package org.example.weightgainapp.service;

import org.example.weightgainapp.dto.DashboardDto;
import org.example.weightgainapp.entity.Food;
import org.example.weightgainapp.entity.User;
import org.example.weightgainapp.repository.FoodRepository;
import org.example.weightgainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    // 特定のユーザーの食事リストを取得
    public List<Food> getFoodsByUser(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return foodRepository.findByUserId(user.getId());
    }

    public DashboardDto getDashboardData(String username) {
        // リストを取得
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Food> foods = foodRepository.findByUserId(user.getId());

        // 計算ロジック
        int totalCalories = foods.stream().mapToInt(Food::getCalories).sum();

        int targetCalories = (user.getTargetCalories() != null)
                ? user.getTargetCalories()
                : 2500;

        int remainingCalories = Math.max(0, targetCalories - totalCalories);

        int progress = 0;
        if (targetCalories > 0) {
            progress = (int) ((double) totalCalories / targetCalories * 100);
            if (progress > 100) progress = 100; // 100%を超えないように
        }

        // 箱(DTO)に詰めて返す
        return DashboardDto.builder()
                .foods(foods)
                .totalCalories(totalCalories)
                .targetCalories(targetCalories)
                .remainingCalories(remainingCalories)
                .progress(progress)
                .build();
    }

    // 食事を記録すsる
    @Transactional
    public void addFood(String username, String name, Integer calories) {
        User user = userRepository.findByUsername(username).orElseThrow();

        Food food = new Food();
        food.setName(name);
        food.setCalories(calories);
        food.setUserId(user.getId());
        food.setEatenAt(LocalDateTime.now());

        foodRepository.save(food);
    }

    // 自分のデータなら削除できる
    @Transactional
    public void deleteFood(String username, Long foodId) {
        User user = userRepository.findByUsername(username).orElseThrow();
        Food food = foodRepository.findById(foodId).orElseThrow();

        // 他人のデータを消せないようにチェック
        if (food.getUserId().equals(user.getId())) {
            foodRepository.delete(food);
        }
    }
}
