package org.example.futoru.controller;

import org.example.futoru.entity.User;
import org.example.futoru.service.UserService;
import org.example.futoru.repository.UserRepository;
import org.example.futoru.dto.ActivityLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final UserService userService;

    // 初回設定画面を表示
    @GetMapping("/profile/init")
    public String showProfileInitForm() {
        return "profile-init"; // templates/profile-init.html を表示
    }

    // 初回設定の保存処理
    @PostMapping("/profile/init")
    public String initProfile(
            @RequestParam Double height,
            @RequestParam Double weight,
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam ActivityLevel activityLevel,
            Principal principal
    ) {
        userService.saveInitialProfile(principal.getName(), height, weight, age, gender, activityLevel);

        return "redirect:/";
    }

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
            @RequestParam Integer age,
            @RequestParam String gender,
            @RequestParam ActivityLevel activityLevel,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        userService.updateProfile(principal.getName(), height, age, gender, activityLevel);
        redirectAttributes.addFlashAttribute("successMessage", "プロフィールを更新しました！");
        return "redirect:/profile";
    }
}
