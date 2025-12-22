package org.example.weightgainapp.controller;

import org.example.weightgainapp.entity.User;
import org.example.weightgainapp.service.UserService;
import org.example.weightgainapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final UserService userService;

    // プロフィール編集画面を表示
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();

        model.addAttribute("user", user);
        return "profile";
    }

    // プロフィール更新処理
    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam Double height,
            @RequestParam Double weight,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam Integer activityLevel,
            Principal principal
    ) {
        userService.updateProfile(principal.getName(), height, weight, age, gender, activityLevel);
        return "redirect:/profile";
    }
}
