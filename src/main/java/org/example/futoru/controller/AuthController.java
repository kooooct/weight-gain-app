package org.example.futoru.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.futoru.service.UserService;
import org.example.futoru.form.RegisterForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * ユーザー認証（ログイン・登録）に関するリクエストを制御するコントローラー。
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /**
     * ユーザー新規登録画面を表示する。
     *
     * @return 登録画面テンプレート名 ("register")
     */
    @GetMapping("/register")
    public String showRegisterForm(@AuthenticationPrincipal UserDetails userDetails,
                                   @ModelAttribute("registerForm") RegisterForm form) {
        if (userDetails != null) {
            return "redirect:/";
        }
        return "register";
    }

    /**
     * ユーザーの新規登録処理を実行する。
     * 入力された情報でユーザーを作成し、成功時はプロフィール登録画面へリダイレクトする。
     * 重複エラー時は登録画面に戻し、メッセージを表示する。
     *
     * @return プロフィール登録画面へのリダイレクトパス
     */
    @PostMapping("/register")
    public String register(@Validated @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes
    ) {
        // バリデーションエラー（文字数など）があるか？
        if (bindingResult.hasErrors()) {
            return "register";
        }

        // パスワード一致チェック（カスタムエラーとして追加）
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            // "confirmPassword" フィールドにエラーを追加する
            bindingResult.rejectValue("confirmPassword", "error.password.match", "パスワード（確認用）が一致しません");
            return "register";
        }
        try {
            userService.registerUser(form.getUsername(), form.getPassword());

            // --- 登録後の自動ログイン処理 ---

            // 通常、registerUserだけではDBに保存されるだけでログイン状態にはならない。
            // UX向上のため、ここで強制的にログイン処理（自動ログイン）を実行する。

            // 1. 今作ったばかりのユーザー情報をDBから取得
            UserDetails userDetails = userService.loadUserByUsername(form.getUsername());

            // 2. 認証トークンを作成
            // ユーザー情報、パスワード、権限（ROLE_USER等）をセットにする
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    form.getPassword(),
                    userDetails.getAuthorities()
            );

            // 3. セキュリティコンテキストに手形を入れる
            // createEmptyContext()を使うのは、マルチスレッド環境での競合（Race Condition）を防ぐため
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // 4. セッションに明示的に保存する
            // Spring Security 6系からは、これを書かないとリクエスト終了時に
            // コンテキストの中身が破棄されてしまい、ログインが維持できない
            securityContextRepository.saveContext(context, request, response);

            redirectAttributes.addFlashAttribute("toastMessage", "ようこそ、Futoruへ！");

            return "redirect:/profile/init";

        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("username", "error.user.duplicate", "このユーザIDは既に使用されています。");
            return "register";
        }
    }

    /**
     * カスタムログイン画面を表示する。
     * SecurityConfigにて loginPage("/login") が設定されている場合に呼び出される。
     *
     * @return ログイン画面テンプレート名 ("login")
     */
    @GetMapping("/login")
    public String showLoginForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";
        }
        return "login";
    }
}