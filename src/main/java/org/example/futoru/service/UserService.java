package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.ActivityLevel;
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
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException();
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // デフォルト権限

        // その他のフィールドはnullのままでOK
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
     * @param activityLevel 活動レベル (Enum: LOW, MID, HIGH)
     * @throws RuntimeException 指定されたユーザーが見つからない場合
     */
    @Transactional
    public void updateProfile(String username, Double height, Double weight, Integer age, String gender, ActivityLevel activityLevel) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setHeight(height);
        user.setWeight(weight);
        user.setAge(age);
        user.setGender(gender);
        user.setActivityLevel(activityLevel.name());

        int targetCalories = calculateTargetCalories(height, weight, age, gender, activityLevel);
        user.setTargetCalories(targetCalories);

        userRepository.save(user);
    }

    /**
     * プロフィール情報から目標カロリーを算出する内部メソッド。
     * 基礎代謝(BMR) × 活動レベル係数 + 増量分(300kcal) で計算する。
     * @param activityLevel 活動レベル
     */
    private int calculateTargetCalories(Double height, Double weight, Integer age, String gender, ActivityLevel activityLevel) {
        double bmr;
        // Mifflin-St Jeor式を使用
        if ("MALE".equals(gender)) {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        } else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        double tdee;
        if (activityLevel == null) activityLevel = ActivityLevel.LOW;

        switch (activityLevel) {
            case LOW:
                tdee = bmr * 1.2;
                break;
            case MID:
                tdee = bmr * 1.55;
                break;
            case HIGH:
                tdee = bmr * 1.725;
                break;
            default:
                tdee = bmr * 1.2;
        }

        return (int) (tdee + 300);
    }
}