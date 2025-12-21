package org.example.weightgainapp.dto;

import lombok.Data;

@Data
public class BmrResponse {
    private Double bmr;
    private Double tdee;
    private Double targetCalories;
    private String description;
}
