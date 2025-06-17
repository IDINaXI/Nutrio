package com.nutrio.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Data;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Entity
@Table(name = "meal_plans")
@Schema(description = "Weekly meal plan for a user")
public class MealPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the meal plan", example = "1")
    private Long id;

    @Column(name = "user_id", insertable = false, updatable = false)
    @Schema(description = "User this meal plan belongs to")
    private Long userId;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Plan description")
    private String plan;

    @Column(name = "created_at")
    @Schema(description = "Creation date and time")
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Schema(description = "User this meal plan belongs to")
    private User user;

    @Column(nullable = false)
    @Schema(description = "Start date of the meal plan", example = "2024-05-01")
    private LocalDate startDate;

    @Column(nullable = false)
    @Schema(description = "End date of the meal plan", example = "2024-05-07")
    private LocalDate endDate;

    @Column(nullable = false)
    @Schema(description = "Total calories for the week", example = "14000")
    private int totalCalories;

    @Column(nullable = false)
    @Schema(description = "Total protein content in grams for the week", example = "525.0")
    private double totalProteins;

    @Column(nullable = false)
    @Schema(description = "Total fat content in grams for the week", example = "350.0")
    private double totalFats;

    @Column(nullable = false)
    @Schema(description = "Total carbohydrate content in grams for the week", example = "1400.0")
    private double totalCarbohydrates;

    @Column(columnDefinition = "LONGTEXT")
    private String dailyMeals;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "meal_plan_breakfast", joinColumns = @JoinColumn(name = "meal_plan_id"))
    private List<Meal> breakfast;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "meal_plan_lunch", joinColumns = @JoinColumn(name = "meal_plan_id"))
    private List<Meal> lunch;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "meal_plan_dinner", joinColumns = @JoinColumn(name = "meal_plan_id"))
    private List<Meal> dinner;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "meal_plan_snacks", joinColumns = @JoinColumn(name = "meal_plan_id"))
    private List<Meal> snacks;

    @Embedded
    private Macronutrients macronutrients;

    @Column(columnDefinition = "LONGTEXT")
    private String week;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Embeddable
    public static class Macronutrients {
        @Column(name = "proteins")
        private double proteins;

        @Column(name = "fats")
        private double fats;

        @Column(name = "carbs")
        private double carbs;

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

        public double getCarbs() {
            return carbs;
        }

        public void setCarbs(double carbs) {
            this.carbs = carbs;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Конструкторы
    public MealPlan() {
    }

    public MealPlan(User user, LocalDate startDate, LocalDate endDate, 
                   int totalCalories, double totalProteins, double totalFats, 
                   double totalCarbohydrates, Map<LocalDate, List<Meal>> dailyMeals) {
        this.user = user;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCalories = totalCalories;
        this.totalProteins = totalProteins;
        this.totalFats = totalFats;
        this.totalCarbohydrates = totalCarbohydrates;
        setDailyMeals(dailyMeals);
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = user != null ? user.getId() : null;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getTotalCalories() {
        return totalCalories;
    }

    public void setTotalCalories(int totalCalories) {
        this.totalCalories = totalCalories;
    }

    public double getTotalProteins() {
        return totalProteins;
    }

    public void setTotalProteins(double totalProteins) {
        this.totalProteins = totalProteins;
    }

    public double getTotalFats() {
        return totalFats;
    }

    public void setTotalFats(double totalFats) {
        this.totalFats = totalFats;
    }

    public double getTotalCarbohydrates() {
        return totalCarbohydrates;
    }

    public void setTotalCarbohydrates(double totalCarbohydrates) {
        this.totalCarbohydrates = totalCarbohydrates;
    }

    public Map<LocalDate, List<Meal>> getDailyMeals() {
        if (dailyMeals == null) return null;
        try {
            return objectMapper.readValue(dailyMeals, new TypeReference<Map<LocalDate, List<Meal>>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    public void setDailyMeals(Map<LocalDate, List<Meal>> dailyMeals) {
        try {
            this.dailyMeals = objectMapper.writeValueAsString(dailyMeals);
        } catch (Exception e) {
            this.dailyMeals = null;
        }
    }

    public List<Meal> getBreakfast() {
        return breakfast;
    }

    public void setBreakfast(List<Meal> breakfast) {
        this.breakfast = breakfast;
    }

    public List<Meal> getLunch() {
        return lunch;
    }

    public void setLunch(List<Meal> lunch) {
        this.lunch = lunch;
    }

    public List<Meal> getDinner() {
        return dinner;
    }

    public void setDinner(List<Meal> dinner) {
        this.dinner = dinner;
    }

    public List<Meal> getSnacks() {
        return snacks;
    }

    public void setSnacks(List<Meal> snacks) {
        this.snacks = snacks;
    }

    public Macronutrients getMacronutrients() {
        return macronutrients;
    }

    public void setMacronutrients(Macronutrients macronutrients) {
        this.macronutrients = macronutrients;
    }

    public List<WeeklyDayPlan> getWeek() {
        if (week == null) return null;
        try {
            List<WeeklyDayPlan> weekList = objectMapper.readValue(week, new TypeReference<List<WeeklyDayPlan>>() {});
            String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
            for (int i = 0; i < weekList.size(); i++) {
                WeeklyDayPlan dayPlan = weekList.get(i);
                if (dayPlan.getDate() == null || dayPlan.getDate().isEmpty()) {
                    dayPlan.setDate(days[i % 7]);
                }
            }
            return weekList;
        } catch (Exception e) {
            return null;
        }
    }

    public void setWeek(List<WeeklyDayPlan> week) {
        try {
            this.week = objectMapper.writeValueAsString(week);
        } catch (Exception e) {
            this.week = null;
        }
    }

    public static class WeeklyDayPlan {
        private String date;
        private Meal breakfast;
        private Meal lunch;
        private Meal dinner;
        private Meal snack;
        private int totalCalories;
        private Macronutrients macronutrients;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Meal getBreakfast() {
            return breakfast;
        }

        public void setBreakfast(Meal breakfast) {
            this.breakfast = breakfast;
        }

        public Meal getLunch() {
            return lunch;
        }

        public void setLunch(Meal lunch) {
            this.lunch = lunch;
        }

        public Meal getDinner() {
            return dinner;
        }

        public void setDinner(Meal dinner) {
            this.dinner = dinner;
        }

        public Meal getSnack() {
            return snack;
        }

        public void setSnack(Meal snack) {
            this.snack = snack;
        }

        public int getTotalCalories() {
            return totalCalories;
        }

        public void setTotalCalories(int totalCalories) {
            this.totalCalories = totalCalories;
        }

        public Macronutrients getMacronutrients() {
            return macronutrients;
        }

        public void setMacronutrients(Macronutrients macronutrients) {
            this.macronutrients = macronutrients;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MealPlan mealPlan = (MealPlan) o;
        return Objects.equals(id, mealPlan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}