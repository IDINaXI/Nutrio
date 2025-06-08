package com.nutrio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrio.model.Meal;
import com.nutrio.model.MealPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class MealPlanParser {
    private static final Logger logger = LoggerFactory.getLogger(MealPlanParser.class);
    private final ObjectMapper objectMapper;

    public MealPlanParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public MealPlan parseResponse(String aiResponse) {
        try {
            logger.debug("Parsing AI response: {}", aiResponse);
            
            // Clean the response from any markdown formatting
            String cleanJson = aiResponse.replaceAll("```json\\s*", "").replaceAll("```\\s*$", "").trim();
            
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            JsonNode mealsNode = rootNode.get("meals");
            
            if (mealsNode == null || !mealsNode.isArray()) {
                throw new RuntimeException("Invalid response format: 'meals' array not found");
            }

            Map<LocalDate, List<Meal>> dailyMeals = new HashMap<>();
            LocalDate startDate = LocalDate.now();

            for (JsonNode mealNode : mealsNode) {
                try {
                    Meal meal = new Meal();
                    meal.setName(mealNode.get("name").asText());
                    meal.setMealType(Meal.MealType.valueOf(mealNode.get("mealType").asText().toUpperCase()));
                    
                    // Parse ingredients as a list
                    String ingredientsStr = mealNode.get("ingredients").asText();
                    List<String> ingredients = Arrays.asList(ingredientsStr.split(",\\s*"));
                    meal.setIngredients(ingredients);
                    
                    // Use Number for numeric values to avoid type casting issues
                    Number calories = mealNode.get("calories").numberValue();
                    Number proteins = mealNode.get("proteins").numberValue();
                    Number fats = mealNode.get("fats").numberValue();
                    Number carbs = mealNode.get("carbs").numberValue();
                    
                    meal.setCalories(calories.intValue());
                    meal.setProteins(proteins.doubleValue());
                    meal.setFats(fats.doubleValue());
                    
                    // Calculate the date based on the day number
                    int dayNumber = mealNode.get("day").asInt();
                    LocalDate mealDate = startDate.plusDays(dayNumber - 1);
                    
                    // Add meal to the appropriate day's list
                    dailyMeals.computeIfAbsent(mealDate, k -> new ArrayList<>()).add(meal);
                } catch (Exception e) {
                    logger.error("Error parsing meal: {}", mealNode, e);
                    throw new RuntimeException("Failed to parse meal: " + e.getMessage());
                }
            }

            if (dailyMeals.isEmpty()) {
                throw new RuntimeException("No meals found in the response");
            }

            MealPlan mealPlan = new MealPlan();
            mealPlan.setStartDate(startDate);
            mealPlan.setEndDate(startDate.plusDays(6));
            mealPlan.setDailyMeals(dailyMeals);

            return mealPlan;
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to parse meal plan: " + e.getMessage(), e);
        }
    }
} 