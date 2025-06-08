package com.nutrio.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class BodyMeasurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate date;
    private Double waist;   // Талия
    private Double chest;   // Грудь
    private Double hips;    // Бёдра
    private Double arm;     // Рука
    private Double leg;     // Нога

    public BodyMeasurement() {}

    public BodyMeasurement(Long userId, LocalDate date, Double waist, Double chest, Double hips, Double arm, Double leg) {
        this.userId = userId;
        this.date = date;
        this.waist = waist;
        this.chest = chest;
        this.hips = hips;
        this.arm = arm;
        this.leg = leg;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Double getWaist() { return waist; }
    public void setWaist(Double waist) { this.waist = waist; }

    public Double getChest() { return chest; }
    public void setChest(Double chest) { this.chest = chest; }

    public Double getHips() { return hips; }
    public void setHips(Double hips) { this.hips = hips; }

    public Double getArm() { return arm; }
    public void setArm(Double arm) { this.arm = arm; }

    public Double getLeg() { return leg; }
    public void setLeg(Double leg) { this.leg = leg; }
} 