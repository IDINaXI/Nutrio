package com.nutrio.repository;

import com.nutrio.model.Meal;
import com.nutrio.model.Meal.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MealRepository extends JpaRepository<Meal, Long> {
    List<Meal> findByMealType(MealType mealType);
    List<Meal> findByCaloriesBetween(int minCalories, int maxCalories);
} 
 
 
 