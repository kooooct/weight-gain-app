package org.example.weightgainapp.controller;

import org.example.weightgainapp.entity.Food;
import org.example.weightgainapp.entity.User;
import org.example.weightgainapp.repository.FoodRepository;
import org.example.weightgainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
@Controller
@RequiredArgsConstructor
public class WebController {
    private final FoodRepository foodRepository;
    private final UserRepository userRepository;

    // トップページを表示する (GET /)
    @GetMapping("/")
    public String showTopPage(Model model, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();
        // 1. 食べたものリストを取得
        List<Food> foods = foodRepository.findByUserId(currentUser.getId());

        // 2. 合計カロリーを計算
        int totalCalories = foods.stream().mapToInt(Food::getCalories).sum();

        // 3. 目標カロリー (仮)
        int targetCalories = 2500;

        // 4. 残りカロリーを計算 (マイナスにならないようにMath.maxを使う)
        int remainingCalories = Math.max(0, targetCalories - totalCalories);

        // 5. 達成率（%）を計算 (グラフ用)
        int progress = (int) ((double) totalCalories / targetCalories * 100);
        if (progress > 100) progress = 100; // 100%を超えないように

        // 6. 画面に渡す
        model.addAttribute("foods", foods);
        model.addAttribute("totalCalories", totalCalories);
        model.addAttribute("targetCalories", targetCalories);      // 目標
        model.addAttribute("remainingCalories", remainingCalories); // 残り
        model.addAttribute("progress", progress);                  // 達成率

        return "index";
    }

    @PostMapping("/add")
    public String addFood(@RequestParam String name, @RequestParam Integer calories, Principal principal) {
        String username = principal.getName();
        User currentUser = userRepository.findByUsername(username).orElseThrow();

        Food food = new Food();
        food.setName(name);
        food.setCalories(calories);
        food.onPrePersist();
        food.setUserId(currentUser.getId());
        foodRepository.save(food);

        return "redirect:/";
    }
}
