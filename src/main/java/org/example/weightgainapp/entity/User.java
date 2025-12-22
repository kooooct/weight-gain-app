package org.example.weightgainapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // 重複禁止＆空っぽ禁止
    private String username;

    @Column(nullable = false)
    private String password;

    // 今回はシンプルにするため、権限は "USER" で固定します
    private String role = "USER";

    private Double height; // 身長 (cm)
    private Double weight; // 体重 (kg)
    private Integer age;   // 年齢
    private String gender; // 性別 (MALE / FEMALE)

    // 活動レベル (1:低い, 2:普通, 3:高い)
    private Integer activityLevel;

    // 計算された目標カロリーを保存しておく場所
    private Integer targetCalories;
}