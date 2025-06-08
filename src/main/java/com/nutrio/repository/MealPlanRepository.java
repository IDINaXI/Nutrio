package com.nutrio.repository;

import com.nutrio.model.MealPlan;
import com.nutrio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByUserId(Long userId);

    Optional<MealPlan> findByUserAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        User user, LocalDate startDate, LocalDate endDate);
}
