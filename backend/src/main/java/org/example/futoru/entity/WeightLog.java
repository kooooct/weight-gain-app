package org.example.futoru.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * ユーザーの体重記録を管理するエンティティクラス。
 * <p>
 * データベースの weight_logs テーブルに対応し、
 * ユーザーの日々の体重変化を記録・保持する。
 * </p>
 */
@Entity
@Data
@Table(name = "weight_logs")
public class WeightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** どのユーザーの記録か */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** いつの記録か */
    @Column(nullable = false)
    private LocalDate date;

    /** 何キロだったか */
    @Column(nullable = false)
    private Double weight;
}