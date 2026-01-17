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
import org.springframework.transaction.annotation.Transactional;

/**
 * 基礎代謝(BMR)および増量目標カロリーの計算ロジックを提供するサービスクラス。
 * <p>
 * Mifflin-St Jeor式を用いてBMRを算出し、
 * ActivityLevel（活動レベル）に応じた係数を掛けてTDEE（総エネルギー消費量）を求める。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class BmrService {

    private final UserRepository userRepository;
    private final WeightLogRepository weightLogRepository;

    // === 定数定義 ===

    /** プロフィール未設定時に返却するデフォルトの目標カロリー (kcal) */
    private static final int DEFAULT_TARGET_CALORIES = 2200;

    /** 増量目的のためにTDEEに上乗せする余剰カロリー (kcal) */
    private static final int SURPLUS_CALORIES_FOR_GAIN = 300;

    // --- Mifflin-St Jeor計算式の係数 ---
    private static final double WEIGHT_MULTIPLIER = 10.0;
    private static final double HEIGHT_MULTIPLIER = 6.25;
    private static final double AGE_MULTIPLIER = 5.0;

    // 性別による補正値
    private static final int MALE_OFFSET = 5;
    private static final int FEMALE_OFFSET = -161;

    /**
     * 指定されたユーザーの最新情報（プロフィールおよび最新の体重ログ）に基づき、
     * 1日の目標摂取カロリーを計算して返却する。
     * <p>
     * 体重ログが存在しない、またはプロフィール情報（身長・年齢）が不足している場合は、
     * 安全策としてデフォルト値 {@link #DEFAULT_TARGET_CALORIES} を返す。
     * </p>
     *
     * @param username 計算対象のユーザー名
     * @return 1日の目標摂取カロリー (kcal)
     * @throws UsernameNotFoundException ユーザーが存在しない場合
     */
    @Transactional(readOnly = true)
    public int calculateTargetCalories(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 計算には最新の体重ログを使用する
        Double currentWeight = weightLogRepository.findFirstByUserOrderByDateDesc(user)
                .map(WeightLog::getWeight)
                .orElse(null);

        // 必須パラメータの不足チェック
        if (currentWeight == null || user.getHeight() == null || user.getAge() == null) {
            return DEFAULT_TARGET_CALORIES;
        }

        // 計算用リクエストDTOの構築
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
     * リクエストDTO（身体データ）に基づき、各種指標を計算する。
     * <ul>
     * <li>BMR (基礎代謝)</li>
     * <li>TDEE (総エネルギー消費量)</li>
     * <li>Target Calories (増量用目標カロリー)</li>
     * </ul>
     *
     * @param request 計算用パラメータ (身長, 体重, 年齢, 性別, 活動レベル)
     * @return 計算結果を含むレスポンスDTO
     */
    public BmrResponse calculate(BmrRequest request) {
        double bmr = calculateBmr(request);
        double tdee = calculateTdee(bmr, request.getActivityLevel());
        double targetCalories = tdee + SURPLUS_CALORIES_FOR_GAIN;

        BmrResponse response = new BmrResponse();
        // 表示用に小数点第1位で四捨五入を行う
        response.setBmr(Math.round(bmr * 10.0) / 10.0);
        response.setTdee(Math.round(tdee * 10.0) / 10.0);
        response.setTargetCalories(Math.round(targetCalories * 10.0) / 10.0);
        response.setDescription(createAdviceMessage(targetCalories));

        return response;
    }

    /**
     * Mifflin-St Jeor式を用いて基礎代謝(BMR)を計算する。
     * <br>
     * 計算式: (10 × 体重kg) + (6.25 × 身長cm) - (5 × 年齢) + 性別補正値
     *
     * @param req 身体データ
     * @return 基礎代謝量 (kcal)
     */
    private double calculateBmr(BmrRequest req) {
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
     * <p>
     * {@link ActivityLevel} Enumに定義された倍率(multiplier)を使用する。
     * </p>
     *
     * @param bmr   基礎代謝量
     * @param level 活動レベル
     * @return TDEE (kcal)
     */
    private double calculateTdee(double bmr, ActivityLevel level) {
        if (level == null) {
            // 未設定の場合は最も低い活動レベルの係数を適用する
            return bmr * ActivityLevel.LOW.getMultiplier();
        }
        return bmr * level.getMultiplier();
    }

    /**
     * ユーザーへのアドバイスメッセージを作成する。
     *
     * @param target 目標摂取カロリー
     * @return メッセージ文字列
     */
    private String createAdviceMessage(double target) {
        return "太るためには、1日約 " + (int) target + "kcal を目指して食べましょう！";
    }

    /**
     * DB保存値(String)をGender Enumに変換する。
     * nullまたは不正値の場合はデフォルトでMALEを返す。
     */
    private Gender convertGender(String genderStr) {
        if (genderStr == null) return Gender.MALE;
        if (genderStr.equalsIgnoreCase("MALE") || genderStr.equals("男性")) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }

    /**
     * DB保存値(String)をActivityLevel Enumに変換する。
     * 変換失敗時はデフォルトでLOWを返す。
     */
    private ActivityLevel convertActivityLevel(String levelStr) {
        if (levelStr == null) return ActivityLevel.LOW;
        try {
            return ActivityLevel.valueOf(levelStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ActivityLevel.LOW;
        }
    }
}