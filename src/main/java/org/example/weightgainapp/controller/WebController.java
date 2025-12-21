package org.example.weightgainapp.controller;

import org.example.weightgainapp.entity.Food;
import org.example.weightgainapp.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
@Controller
@RequiredArgsConstructor
public class WebController {
    private final FoodRepository foodRepository;

    // トップページを表示する (GET /)
    @GetMapping("/")
    public String showTopPage(Model model) {
        // 1. DBから食べたものリストを全部持ってくる
        List<Food> foods = foodRepository.findAll();

        // 2. 合計カロリーを計算する (JavaのStream APIを使用)
        // リストの中の food から getCalories を取り出して、全部足す(sum)
        int totalCalories = foods.stream()
                .mapToInt(Food::getCalories)
                .sum();

        // 3. 画面(HTML)にデータを渡す
        model.addAttribute("foods", foods);
        model.addAttribute("totalCalories", totalCalories); // ← これを追加！

        return "index";
    }

    @PostMapping
    public String addFood(@RequestParam String name, @RequestParam Integer calories) {
        Food food = new Food();
        food.setName(name);
        food.setCalories(calories);
        food.onPrePersist();
        foodRepository.save(food);

        return "redirect:/";
    }
}
