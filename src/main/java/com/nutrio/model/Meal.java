package com.nutrio.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "meals")
@Schema(description = "Meal entity representing a single meal in the nutrition plan")
public class Meal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the meal", example = "1")
    private Long id;

    @Column(nullable = false, length = 100)
    @Schema(description = "Name of the meal", example = "Oatmeal with Berries")
    private String name;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Schema(description = "Type of meal", example = "BREAKFAST")
    private MealType mealType;

    @Column(nullable = false)
    @Schema(description = "Calories in the meal", example = "350")
    private int calories;

    @Column(nullable = false)
    @Schema(description = "Protein content in grams", example = "15.5")
    private double proteins;

    @Column(nullable = false)
    @Schema(description = "Fat content in grams", example = "8.2")
    private double fats;

    @Column(nullable = false)
    @Schema(description = "Carbohydrate content in grams", example = "45.3")
    private double carbohydrates;

    @ElementCollection
    @CollectionTable(name = "meal_ingredients", joinColumns = @JoinColumn(name = "meal_id"))
    @Column(name = "ingredient")
    @Schema(description = "List of ingredients in the meal", example = "[\"oats\", \"berries\", \"milk\"]")
    private List<String> ingredients;

    @Column(length = 1000)
    @Schema(description = "Recipe instructions for preparing the meal", example = "1. Cook oats in milk...")
    private String recipe;

    @Column(length = 500)
    @Schema(description = "Description of the meal", example = "A hearty breakfast with oats and fresh berries")
    private String description;

    @Column(length = 255)
    @Schema(description = "URL of the meal image", example = "https://example.com/meal.jpg")
    private String imageUrl;

    @Schema(description = "Type of meal in the daily nutrition plan")
    public enum MealType {
        @Schema(description = "Breakfast meal")
        BREAKFAST,
        @Schema(description = "Lunch meal")
        LUNCH,
        @Schema(description = "Dinner meal")
        DINNER,
        @Schema(description = "Snack between meals")
        SNACK
    }

    // Constructors
    public Meal() {
    }

    public Meal(String name, MealType mealType, int calories, double proteins, 
                double fats, double carbohydrates, List<String> ingredients, String recipe) {
        this.name = name;
        this.mealType = mealType;
        this.calories = calories;
        this.proteins = proteins;
        this.fats = fats;
        this.carbohydrates = carbohydrates;
        this.ingredients = ingredients;
        this.recipe = recipe;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getProteins() {
        return proteins;
    }

    public void setProteins(double proteins) {
        this.proteins = proteins;
    }

    public double getFats() {
        return fats;
    }

    public void setFats(double fats) {
        this.fats = fats;
    }

    public double getCarbohydrates() {
        return carbohydrates;
    }

    public void setCarbohydrates(double carbohydrates) {
        this.carbohydrates = carbohydrates;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public String getRecipe() {
        return recipe;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 