package org.example.futoru.controller;

import org.example.futoru.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ユーザー認証（ログイン・登録）に関するリクエストを制御するコントローラー。
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * ユーザー新規登録画面を表示する。
     *
     * @return 登録画面テンプレート名 ("register")
     */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    /**
     * ユーザーの新規登録処理を実行する。
     * 入力された情報でユーザーを作成し、成功時はログイン画面へリダイレクトする。
     *
     * @param username 登録するユーザー名
     * @param password 登録するパスワード（平文）
     * @return ログイン画面へのリダイレクトパス
     */
    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String password) {
        userService.registerUser(username, password);
        return "redirect:/login";
    }

    /**
     * カスタムログイン画面を表示する。
     * SecurityConfigにて loginPage("/login") が設定されている場合に呼び出される。
     *
     * @return ログイン画面テンプレート名 ("login")
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
}