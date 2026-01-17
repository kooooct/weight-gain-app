package org.example.futoru.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.futoru.entity.User;
import org.example.futoru.entity.WeightLog;
import org.example.futoru.repository.UserRepository;
import org.example.futoru.repository.WeightLogRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@RestController // ← これがつくと、戻り値が自動的に JSON になる！
@RequestMapping("/api/weight")
@RequiredArgsConstructor
public class WeightApiController {

    private final WeightLogRepository weightLogRepository;
    private final UserRepository userRepository;

    /**
     * 指定された日付の体重データを返す
     * URL例: /api/weight?date=2024-01-15
     */
    @GetMapping
    public Map<String, Object> getWeight(
            @RequestParam("date") String dateStr,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String username = userDetails.getUsername();
        User user = userRepository.findByUsername(username).orElseThrow();
        LocalDate date = LocalDate.parse(dateStr);

        Optional<WeightLog> log = weightLogRepository.findByUserAndDate(user, date);

        if (log.isPresent()) {
            // データがあったら JSON で返す { "weight": 60.5 }
            return Map.of("weight", log.get().getWeight());
        } else {
            // なかったら null を返す { "weight": null }
            return Map.of("weight",  null);
        }
    }
}