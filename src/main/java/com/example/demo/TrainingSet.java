// TrainingSet.java (セット詳細)
package com.example.demo;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Data;

@Entity @Data
public class TrainingSet {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer number;
    private Double weight = 0.0;
    private Integer reps = 0;
    private Integer assistReps = 0;

    @ManyToOne @JoinColumn(name = "training_log_id")
    private TrainingLog trainingLog;
}