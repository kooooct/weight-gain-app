package org.example.futoru.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.futoru.service.WeightLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 体重データに関するAPI操作を提供するコントローラークラス。
 * <p>
 * 画面遷移を伴わない非同期通信（AJAX）向けに、JSON形式でデータを返却するエンドポイントを定義する。
 * </p>
 */
@RestController
@RequestMapping("/api/weight")
@RequiredArgsConstructor
public class WeightApiController {

    private final WeightLogService weightLogService;

    /**
     * 指定された日付の体重データを取得する。
     * <p>
     * クライアントから日付を受け取り、該当する体重記録が存在すればその値を返す。
     * カレンダーの日付クリック時などに、既存の記録を表示するために使用される。
     * </p>
     *
     * @param dateStr     日付文字列 (形式: "yyyy-MM-dd")
     * @param userDetails 認証済みユーザー情報
     * @return 体重データを含むMap（キー: "weight", 値: Double または null）
     */
    @GetMapping
    public Map<String, Object> getWeight(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        String username = userDetails.getUsername();

        Double weight = weightLogService.getWeightByDate(username, date);

        Map<String, Object> response = new HashMap<>();
        response.put("weight", weight);

        return response;
    }

    @PostMapping("/add") // URLは /api/weight/add になります
    public ResponseEntity<Map<String, String>> addWeight(
            @RequestParam("date") String dateStr,
            @RequestParam("weight") Double weight,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        LocalDate date = LocalDate.parse(dateStr);
        String username = userDetails.getUsername();

        // 保存処理
        weightLogService.saveWeightLog(username, date, weight);

        // リダイレクトではなく、成功メッセージ(JSON)を返す
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}