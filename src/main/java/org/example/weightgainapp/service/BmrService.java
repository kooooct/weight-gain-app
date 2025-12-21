package org.example.weightgainapp.service;

import org.example.weightgainapp.dto.ActivityLevel;
import org.example.weightgainapp.dto.BmrRequest;
import org.example.weightgainapp.dto.BmrResponse;
import org.example.weightgainapp.dto.Gender;
import org.springframework.stereotype.Service;

@Service
public class BmrService {
    public BmrResponse calculate(BmrRequest request){
        double bmr = calculateBmr(request); //基礎代謝(bmr)を計算
        double tdee = calculateTdee(bmr, request.getActivityLevel()); //メンテナンスカロリー(tdee)を計算
        double targetCalories = tdee + 500; //目標カロリー

        BmrResponse response = new BmrResponse();
        response.setBmr(Math.round(bmr * 10.0) / 10.0);
        response.setTdee(Math.round(tdee * 10.0) / 10.0);
        response.setTargetCalories(Math.round(targetCalories * 10.0) / 10.0);
        response.setDescription(createAdviceMessage(targetCalories));

        return response;
    }

    private double calculateBmr(BmrRequest req){    //ミフリン・セントジョール式でbmr算出
        double baseResult = (10 * req.getWeight()) + (6.25 * req.getHeight()) - (5 * req.getAge());

        // 性別による補正
        if (req.getGender() == Gender.MALE) {
            return baseResult + 5;
        } else {
            return baseResult - 161;
        }
    }

    //活動に応じた係数をかける
    private double calculateTdee(double bmr, ActivityLevel level) {
        switch (level) {
            case LOW:
                return bmr * 1.2;   // ほぼ運動しない
            case MID:
                return bmr * 1.55;  // 適度な運動
            case HIGH:
                return bmr * 1.725; // 激しい運動
            default:
                return bmr * 1.2;
        }
    }

    // 応援メッセージを作成
    private String createAdviceMessage(double target) {
        return "太るためには、1日約 " + (int)target + "kcal を目指して食べましょう！Futoruと一緒に頑張りましょう。";
    }
}
