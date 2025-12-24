package org.example.weightgainapp.controller;

import org.example.weightgainapp.dto.DashboardDto;
import org.example.weightgainapp.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
@Controller
@RequiredArgsConstructor
public class WebController {
    private final FoodService foodService;

    // トップページを表示する (GET /)
    @GetMapping("/")
    public String showTopPage(Model model, Principal principal) {
        DashboardDto dashboard = foodService.getDashboardData(principal.getName());

        model.addAttribute("foods", dashboard.getFoods());
        model.addAttribute("totalCalories", dashboard.getTotalCalories());
        model.addAttribute("targetCalories", dashboard.getTargetCalories());      // 目標
        model.addAttribute("remainingCalories", dashboard.getRemainingCalories()); // 残り
        model.addAttribute("progress", dashboard.getProgress());                  // 達成率

        return "index";
    }

    @PostMapping("/add")
    public String addFood(@RequestParam String name, @RequestParam Integer calories, Principal principal) {
       // 食事を追加
        foodService.addFood(principal.getName(), name, calories);

        return "redirect:/";
    }

    // 削除処理
    @PostMapping("/delete")
    public String deleteFood(@RequestParam Long id, Principal principal) {
        foodService.deleteFood(principal.getName(), id);
        return "redirect:/";
    }
}
