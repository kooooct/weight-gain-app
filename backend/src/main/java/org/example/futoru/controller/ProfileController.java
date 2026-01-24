package org.example.futoru.controller;

import org.example.futoru.entity.User;
import org.example.futoru.service.UserService;
import org.example.futoru.dto.ActivityLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * ユーザープロフィールの表示および編集を管理するコントローラー。
 * <p>
 * アカウント作成直後の「初期設定（身長・体重など）」画面や、
 * 運用開始後の「プロフィール編集」画面の制御を行う。
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * プロフィール初期設定画面を表示する。
     * <p>
     * ユーザー登録直後にリダイレクトされる画面。
     * BMR（基礎代謝）計算に必要な身体データの入力を求める。
     * </p>
     *
     * @return 初期設定画面テンプレート名 ("profile-init")
     */
    @GetMapping("/profile/init")
    public String showProfileInitForm() {
        return "profile-init";
    }

    /**
     * プロフィール初期設定の保存処理を行う。
     * <p>
     * 入力された身体データを保存し、同時に基礎代謝や目標カロリーの計算もサービス側で行う。
     * 処理完了後はトップページへリダイレクトする。
     * </p>
     *
     * @param height        身長 (cm)
     * @param weight        体重 (kg)
     * @param age           年齢
     * @param gender        性別
     * @param activityLevel 活動レベル（Enumとして受け取る）
     * @param principal     認証情報（ユーザー名の取得用）
     * @return トップページへのリダイレクトパス
     */
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

    /**
     * プロフィール編集画面を表示する。
     * <p>
     * 現在のユーザー情報を取得し、フォームに初期値として表示する。
     * </p>
     *
     * @param principal 認証情報
     * @param model     画面表示用モデル
     * @return プロフィール画面テンプレート名 ("profile")
     */
    @GetMapping("/profile")
    public String showProfile(Principal principal, Model model) {
        String username = principal.getName();

        User user = userService.getUserByUsername(username);

        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * プロフィールの更新処理を行う。
     * <p>
     * 身長、年齢、活動レベルなどの基本情報を更新する。
     * 体重に関しては日々のログ機能（WeightLog）で管理するため、ここでの更新対象からは除外している。
     * </p>
     *
     * @param height             身長
     * @param age                年齢
     * @param gender             性別
     * @param activityLevel      活動レベル
     * @param principal          認証情報
     * @param redirectAttributes 完了メッセージをリダイレクト先に渡すためのオブジェクト
     * @return プロフィール画面へのリダイレクトパス
     */
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

        // フラッシュメッセージを設定
        redirectAttributes.addFlashAttribute("successMessage", "プロフィールを更新しました！");

        return "redirect:/profile";
    }
}