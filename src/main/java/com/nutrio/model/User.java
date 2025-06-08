package com.nutrio.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Schema(description = "User entity representing a Nutrio user")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user", example = "1")
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    @Schema(description = "User's email address", example = "user@example.com")
    private String email;

    @Column(nullable = false, length = 200)
    @Schema(description = "User's password (hashed)", example = "$2a$10$...")
    private String password;

    @Column(nullable = false, length = 50)
    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Column(nullable = false)
    @Schema(description = "User's age in years", example = "30")
    private int age;

    @Column(nullable = false)
    @Schema(description = "User's height in centimeters", example = "175.0")
    private double height;

    @Column(nullable = false)
    @Schema(description = "User's weight in kilograms", example = "70.0")
    private double weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "User's gender", example = "MALE")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "User's fitness goal", example = "LOSE_WEIGHT")
    private Goal goal;

    @ElementCollection
    @CollectionTable(name = "user_allergies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "allergy")
    @Fetch(FetchMode.JOIN)
    @Schema(description = "List of user's food allergies", example = "[\"nuts\", \"dairy\"]")
    private List<String> allergies;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "User's activity level", example = "SEDENTARY")
    private ActivityLevel activityLevel;

    @Column(name = "last_weight")
    private Double lastWeight;

    @Column(name = "last_weight_date")
    private java.time.LocalDate lastWeightDate;

    @Schema(description = "User's gender")
    public enum Gender {
        @Schema(description = "Male gender")
        MALE,
        @Schema(description = "Female gender")
        FEMALE
    }

    @Schema(description = "User's fitness goal")
    public enum Goal {
        @Schema(description = "Goal to lose weight")
        LOSE_WEIGHT,
        @Schema(description = "Goal to maintain current weight")
        MAINTAIN_WEIGHT,
        @Schema(description = "Goal to gain weight")
        GAIN_WEIGHT
    }

    @Schema(description = "User's activity level")
    public enum ActivityLevel {
        @Schema(description = "Sedentary lifestyle")
        SEDENTARY,
        @Schema(description = "Lightly active lifestyle")
        LIGHTLY_ACTIVE,
        @Schema(description = "Moderately active lifestyle")
        MODERATELY_ACTIVE,
        @Schema(description = "Very active lifestyle")
        VERY_ACTIVE,
        @Schema(description = "Extremely active lifestyle")
        EXTREMELY_ACTIVE
    }

    // UserDetails interface methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Конструкторы
    @JsonCreator
    public User() {
    }

    @JsonCreator
    public User(
            @JsonProperty("name") String name,
            @JsonProperty("email") String email,
            @JsonProperty("age") int age,
            @JsonProperty("height") double height,
            @JsonProperty("weight") double weight,
            @JsonProperty("gender") String gender,
            @JsonProperty("goal") String goal,
            @JsonProperty("activityLevel") String activityLevel,
            @JsonProperty("allergies") List<String> allergies) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.height = height;
        this.weight = weight;
        try {
            this.gender = Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.gender = null;
        }
        try {
            this.goal = Goal.valueOf(goal.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.goal = null;
        }
        try {
            this.activityLevel = ActivityLevel.valueOf(activityLevel.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.activityLevel = null;
        }
        this.allergies = allergies;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public List<String> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<String> allergies) {
        this.allergies = allergies;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public Double getLastWeight() {
        return lastWeight;
    }

    public void setLastWeight(Double lastWeight) {
        this.lastWeight = lastWeight;
    }

    public java.time.LocalDate getLastWeightDate() {
        return lastWeightDate;
    }

    public void setLastWeightDate(java.time.LocalDate lastWeightDate) {
        this.lastWeightDate = lastWeightDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}