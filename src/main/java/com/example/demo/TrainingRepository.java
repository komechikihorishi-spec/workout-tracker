package com.example.demo;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
public interface TrainingRepository extends JpaRepository<TrainingLog, Long> {
	List<TrainingLog> findAllByOrderByCreatedAtDesc();
	List<TrainingLog> findByExerciseOrderByCreatedAtAsc(String exercise);
	@Query("SELECT DISTINCT t.exercise FROM TrainingLog t WHERE t.exercise IS NOT NULL AND t.exercise != ''")
	List<String> findDistinctExercises();
}