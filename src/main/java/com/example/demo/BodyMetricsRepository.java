package com.example.demo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
public interface BodyMetricsRepository extends JpaRepository<BodyMetrics, Long> {
    Optional<BodyMetrics> findByDate(LocalDate date);
    List<BodyMetrics> findAllByOrderByDateAsc();
}