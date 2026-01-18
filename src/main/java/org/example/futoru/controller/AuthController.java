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
 * <p>
 * 新規ユーザーの登録処理、バリデーションチェック、および登録完了後の自動ログイン処理を担当する。
 * また、ログイン済みユーザーが再度ログイン画面等にアクセスした場合のリダイレクト制御も行う。
 * </p>
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    /**
     * ユーザー新規登録画面を表示する。
     * <p>
     * すでにログイン済みのユーザーがアクセスした場合は、トップページへリダイレクトする。
     * </p>
     *
     * @param userDetails ログイン中のユーザー情報（未ログイン時はnull）
     * @param form        登録フォームオブジェクト
     * @return 登録画面テンプレート名 ("register") またはリダイレクトパス
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
     * <p>
     * 入力値のバリデーションとパスワード一致チェックを行い、問題なければユーザーを作成する。
     * 作成後は自動的にログイン処理（セキュリティコンテキストの保存）を行い、プロフィール初期設定画面へリダイレクトする。
     * </p>
     *
     * @param form               入力された登録データ
     * @param bindingResult      バリデーション結果
     * @param model              画面表示用モデル
     * @param request            サーブレットリクエスト
     * @param response           サーブレットレスポンス
     * @param redirectAttributes リダイレクト先へデータを渡すためのオブジェクト
     * @return 成功時は初期設定画面へのリダイレクト、失敗時は登録画面の再表示
     */
    @PostMapping("/register")
    public String register(@Validated @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult bindingResult,
                           Model model,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.password.match", "パスワード（確認用）が一致しません");
            return "register";
        }

        try {
            userService.registerUser(form.getUsername(), form.getPassword());

            UserDetails userDetails = userService.loadUserByUsername(form.getUsername());
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    form.getPassword(),
                    userDetails.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

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
     * <p>
     * すでにログイン済みのユーザーがアクセスした場合は、トップページへリダイレクトする。
     * </p>
     *
     * @param userDetails ログイン中のユーザー情報（未ログイン時はnull）
     * @return ログイン画面テンプレート名 ("login") またはリダイレクトパス
     */
    @GetMapping("/login")
    public String showLoginForm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            return "redirect:/";
        }
        return "login";
    }
}