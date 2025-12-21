package org.example.weightgainapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "foods")
@Data
public class Food {
    @Id // ← 主キー（ID）
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ← IDを自動採番(1, 2, 3...)
    private Long id;

    private String name;    // 料理名 (例: 牛丼)
    private Integer calories; // カロリー (例: 700)

    // 食べた日時 (登録時に自動で現在時刻を入れる)
    private LocalDateTime eatenAt;
    private Long userId;

    @PrePersist // ← 保存する直前に実行されるメソッド
    public void onPrePersist() {
        if (eatenAt == null) {
            eatenAt = LocalDateTime.now();
        }
    }
}
