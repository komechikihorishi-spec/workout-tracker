// TrainingLog.java (種目主情報)
package com.example.demo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import lombok.Data;

@Entity @Data
public class TrainingLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String exercise; 
    private String category; 
    @Column(columnDefinition = "TEXT")
    private String memo; 
    private Integer durationMinutes = 0;
    private boolean completed = false;
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "trainingLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingSet> sets = new ArrayList<>();
}