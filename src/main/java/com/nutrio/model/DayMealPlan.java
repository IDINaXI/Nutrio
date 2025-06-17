package com.nutrio.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class DayMealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Meal breakfast;

    @OneToOne(cascade = CascadeType.ALL)
    private Meal lunch;

    @OneToOne(cascade = CascadeType.ALL)
    private Meal dinner;

    @OneToOne(cascade = CascadeType.ALL)
    private Meal snack;

    private int totalCalories;

    @Embedded
    private MealPlan.Macronutrients macronutrients;

    @ManyToOne
    private User user;

    private LocalDate date = LocalDate.now();

    @Transient
    private String formattedDate;

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Meal getBreakfast() { return breakfast; }
    public void setBreakfast(Meal breakfast) { this.breakfast = breakfast; }
    public Meal getLunch() { return lunch; }
    public void setLunch(Meal lunch) { this.lunch = lunch; }
    public Meal getDinner() { return dinner; }
    public void setDinner(Meal dinner) { this.dinner = dinner; }
    public Meal getSnack() { return snack; }
    public void setSnack(Meal snack) { this.snack = snack; }
    public int getTotalCalories() { return totalCalories; }
    public void setTotalCalories(int totalCalories) { this.totalCalories = totalCalories; }
    public MealPlan.Macronutrients getMacronutrients() { return macronutrients; }
    public void setMacronutrients(MealPlan.Macronutrients macronutrients) { this.macronutrients = macronutrients; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getFormattedDate() { return formattedDate; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }
} 