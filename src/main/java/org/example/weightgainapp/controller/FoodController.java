package org.example.weightgainapp.controller;

import org.example.weightgainapp.entity.Food;
import org.example.weightgainapp.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/foods")
@RequiredArgsConstructor
public class FoodController {
    private final FoodRepository foodRepository;

    // 食べたものを登録するAPI
    // POST /api/v1/foods
    @PostMapping
    public Food create(@RequestBody Food food) {
        return foodRepository.save(food);
    }

    // 食べた履歴を全件取得するAPI
    // GET /api/v1/foods
    @GetMapping
    public List<Food> getAll() {
        return foodRepository.findAll();
    }
}
