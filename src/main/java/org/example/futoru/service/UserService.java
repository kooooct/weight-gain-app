package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.entity.User;
import org.example.futoru.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ユーザー情報の管理（登録、更新、検索）を行うサービスクラス。
 * Spring Securityの認証用インターフェース(UserDetailsService)も兼ねる。
 */
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ログイン時にSpring Securityから呼び出されるメソッド。
     * ユーザー名からDBを検索し、認証用ユーザー情報を返す。
     *
     * @param username 入力されたユーザー名
     * @return UserDetailsオブジェクト（Userエンティティ）
     * @throws UsernameNotFoundException ユーザーが見つからない場合
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * 新規ユーザーを登録する。
     * パスワードはハッシュ化（暗号化）して保存する。
     *
     * @param username ユーザー名
     * @param password 生パスワード（平文）
     */
    public void registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // デフォルト権限

        // その他のフィールドはnullのままでOK（後でプロフィール設定する）
        userRepository.save(user);
    }

    /**
     * ユーザーのプロフィール情報を更新し、それに基づいて目標カロリーを自動再計算して保存する。
     *
     * @param username      更新対象のユーザー名
     * @param height        身長 (cm)
     * @param weight        体重 (kg)
     * @param age           年齢
     * @param gender        性別 ("MALE" または "FEMALE")
     * @param activityLevel 活動レベル (1:低い, 2:普通, 3:高い)
     * @throws RuntimeException 指定されたユーザーが見つからない場合
     */
    @Transactional
    public void updateProfile(String username, Double height, Double weight, Integer age, String gender, Integer activityLevel) {
        // 1. ユーザーを取得 (存在しない場合はエラー)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. データをセット
        user.setHeight(height);
        user.setWeight(weight);
        user.setAge(age);
        user.setGender(gender);
        user.setActivityLevel(String.valueOf(activityLevel)); // Enum変換などは一旦簡易的にStringで保存

        // 3. 目標カロリーを設定する (簡易計算)
        int targetCalories = calculateTargetCalories(height, weight, age, gender, activityLevel);
        user.setTargetCalories(targetCalories);

        // 4. 保存
        userRepository.save(user);
    }

    /**
     * プロフィール情報から目標カロリーを算出する内部メソッド。
     * 基礎代謝(BMR) × 活動レベル係数 + 増量分(300kcal) で計算する。
     * ※ BmrServiceクラスとロジックが重複しているが、現状は独立して計算を行う。
     */
    private int calculateTargetCalories(Double height, Double weight, Integer age, String gender, Integer activityLevel) {
        double bmr;
        // Mifflin-St Jeor式を使用
        if ("MALE".equals(gender)) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        double tdee;
        if (activityLevel == null) activityLevel = 1;

        switch (activityLevel) {
            case 1: tdee = bmr * 1.2; break;  // ほぼ運動しない
            case 2: tdee = bmr * 1.55; break; // 適度な運動
            case 3: tdee = bmr * 1.9; break;  // 激しい運動
            default: tdee = bmr * 1.2;
        }

        return (int) (tdee + 300);
    }
}