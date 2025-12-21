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
}