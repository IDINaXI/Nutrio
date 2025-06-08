package com.nutrio.repository;

import com.nutrio.model.DayMealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DayMealPlanRepository extends JpaRepository<DayMealPlan, Long> {
    List<DayMealPlan> findByUserId(Long userId);
} 