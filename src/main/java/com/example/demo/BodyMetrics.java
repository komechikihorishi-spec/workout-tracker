// BodyMetrics.java (身体指標)
package com.example.demo;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;

@Entity @Data
public class BodyMetrics {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date = LocalDate.now();
    private Double weight = 0.0;
    private Double bodyFat = 0.0;
    private Double muscleMass = 0.0;
    private Double water = 0.0;
    private Double bmi = 0.0;
}