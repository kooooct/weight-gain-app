package org.example.futoru.service;

import lombok.RequiredArgsConstructor;
import org.example.futoru.dto.ActivityLevel;
import org.example.futoru.dto.BmrRequest;
import org.example.futoru.dto.BmrResponse;
import org.example.futoru.dto.Gender;
import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.example.futoru.repository.UserRepository;
import org.example.futoru.repository.WeightLogRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 基礎代謝(BMR)および増量目標カロリーの計算ロジックを提供するサービスクラス。
 * Mifflin-St Jeor式を用いてBMRを算出し、活動レベルに応じた係数を掛けてTDEEを求める。
 */
@Service
@RequiredArgsConstructor
public class BmrService {

    private final UserRepository userRepository;
    private final WeightLogRepository weightLogRepository;

    // === 定数定義 (マジックナンバーの解消) ===

    /** プロフィール未設定時のデフォルト目標カロリー (kcal) */
    private static final int DEFAULT_TARGET_CALORIES = 2200;

    /** 増量のためにTDEEに上乗せする余剰カロリー (kcal) */
    private static final int SURPLUS_CALORIES_FOR_GAIN = 300;

    // Mifflin-St Jeor計算式の係数
    private static final double WEIGHT_MULTIPLIER = 10.0;
    private static final double HEIGHT_MULTIPLIER = 6.25;
    private static final double AGE_MULTIPLIER = 5.0;
    private static final int MALE_OFFSET = 5;
    private static final int FEMALE_OFFSET = -161;

    // 活動レベル係数
    private static final double ACTIVITY_FACTOR_SEDENTARY = 1.2;   // ほぼ運動しない
    private static final double ACTIVITY_FACTOR_LOW = 1.375;       // 軽い運動
    private static final double ACTIVITY_FACTOR_MODERATE = 1.55;   // 中程度の運動
    private static final double ACTIVITY_FACTOR_HIGH = 1.725;      // 激しい運動

    /**
     * 登録済みユーザー情報に基づき、1日の目標摂取カロリーを計算して返却する。
     * ユーザーのプロフィール情報（身長・体重・年齢）が不足している場合は、安全なデフォルト値を返す。
     *
     * @param username 計算対象のユーザー名
     * @return 1日の目標摂取カロリー (kcal)
     */
    public int calculateTargetCalories(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Double currentWeight = weightLogRepository.findFirstByUserOrderByDateDesc(user)
                .map(WeightLog::getWeight)
                .orElse(null);

        if (currentWeight == null || user.getHeight() == null || user.getAge() == null) {
            return DEFAULT_TARGET_CALORIES;
        }

        BmrRequest request = new BmrRequest();
        request.setHeight(user.getHeight());
        request.setWeight(currentWeight);
        request.setAge(user.getAge());
        request.setGender(convertGender(user.getGender()));
        request.setActivityLevel(convertActivityLevel(user.getActivityLevel()));

        BmrResponse response = calculate(request);

        return (int) response.getTargetCalories();
    }

    /**
     * リクエストDTO（身体データ）に基づき、以下の3つの指標を計算する。
     * 1. BMR (基礎代謝)
     * 2. TDEE (総エネルギー消費量)
     * 3. Target Calories (増量用目標カロリー)
     *
     * @param request 計算用パラメータ (身長, 体重, 年齢, 性別, 活動レベル)
     * @return 計算結果を含むレスポンスDTO
     */
    public BmrResponse calculate(BmrRequest request){
        double bmr = calculateBmr(request);
        double tdee = calculateTdee(bmr, request.getActivityLevel());
        double targetCalories = tdee + SURPLUS_CALORIES_FOR_GAIN;

        BmrResponse response = new BmrResponse();
        // 小数点第1位で四捨五入して整形
        response.setBmr(Math.round(bmr * 10.0) / 10.0);
        response.setTdee(Math.round(tdee * 10.0) / 10.0);
        response.setTargetCalories(Math.round(targetCalories * 10.0) / 10.0);
        response.setDescription(createAdviceMessage(targetCalories));

        return response;
    }

    /**
     * Mifflin-St Jeor式を用いて基礎代謝(BMR)を計算する。
     * 式: (10 × 体重kg) + (6.25 × 身長cm) - (5 × 年齢) + s
     */
    private double calculateBmr(BmrRequest req){
        double baseResult = (WEIGHT_MULTIPLIER * req.getWeight())
                + (HEIGHT_MULTIPLIER * req.getHeight())
                - (AGE_MULTIPLIER * req.getAge());

        if (req.getGender() == Gender.MALE) {
            return baseResult + MALE_OFFSET;
        } else {
            return baseResult + FEMALE_OFFSET;
        }
    }

    /**
     * 活動レベルに基づいてTDEE(総エネルギー消費量)を算出する。
     * BMRに活動係数を乗算する。
     */
    private double calculateTdee(double bmr, ActivityLevel level) {
        if (level == null) return bmr * ACTIVITY_FACTOR_SEDENTARY;
        switch (level) {
            case LOW: return bmr * ACTIVITY_FACTOR_LOW;
            case MID: return bmr * ACTIVITY_FACTOR_MODERATE;
            case HIGH: return bmr * ACTIVITY_FACTOR_HIGH;
            default: return bmr * ACTIVITY_FACTOR_SEDENTARY;
        }
    }

    private String createAdviceMessage(double target) {
        return "太るためには、1日約 " + (int)target + "kcal を目指して食べましょう！";
    }

    private Gender convertGender(String genderStr) {
        if (genderStr == null) return Gender.MALE;
        if (genderStr.equalsIgnoreCase("MALE") || genderStr.equals("男性")) return Gender.MALE;
        return Gender.FEMALE;
    }

    private ActivityLevel convertActivityLevel(String levelStr) {
        if (levelStr == null) return ActivityLevel.LOW;
        try {
            return ActivityLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ActivityLevel.LOW;
        }
    }
}