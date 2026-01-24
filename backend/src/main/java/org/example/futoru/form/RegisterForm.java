package org.example.futoru.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ユーザー登録画面からの入力データを受け取るフォームクラス。
 * <p>
 * 入力値の保持と同時に、アノテーションによるバリデーション（入力チェック）ルールを定義する。
 * コントローラーでリクエストを受け取る際に使用される。
 * </p>
 */
@Data
public class RegisterForm {

    /** ユーザーID（3文字以上20文字以内） */
    @NotBlank(message = "ユーザIDを入力してください")
    @Size(min = 3, max = 20, message = "ユーザIDは3文字以上20文字以内で入力してください")
    private String username;

    /** パスワード（8文字以上） */
    @NotBlank(message = "パスワードを入力してください")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください")
    private String password;

    /** 確認用パスワード */
    @NotBlank(message = "確認用パスワードを入力してください")
    private String confirmPassword;
}