package com.nutrio.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class WeightEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Double weight;
    private LocalDate date;

    public WeightEntry() {}

    public WeightEntry(Long userId, Double weight, LocalDate date) {
        this.userId = userId;
        this.weight = weight;
        this.date = date;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}