package org.example.weightgainapp.controller;

import org.example.weightgainapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    // 登録画面を表示する
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    // 登録処理を実行する
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        userService.registerUser(username, password);
        // 登録できたらログイン画面へリダイレクト
        return "redirect:/login";
    }

    // 独自のログイン画面を表示する場合（今はSpring Security標準画面を使うなら不要だが、一応用意）
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // login.htmlを作成する必要があります
    }
}