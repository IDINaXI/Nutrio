package com.nutrio.model;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.List;

@Entity
public class PillReminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String name; // Название таблетки
    private String dosage; // Дозировка (опционально)
    private String comment; // Комментарий (опционально)
    private LocalTime time; // Время приёма
    private boolean active = true;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> daysOfWeek; // 1=Пн, 7=Вс

    public PillReminder() {}

    public PillReminder(Long userId, String name, String dosage, String comment, LocalTime time, List<Integer> daysOfWeek, boolean active) {
        this.userId = userId;
        this.name = name;
        this.dosage = dosage;
        this.comment = comment;
        this.time = time;
        this.daysOfWeek = daysOfWeek;
        this.active = active;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public List<Integer> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<Integer> daysOfWeek) { this.daysOfWeek = daysOfWeek; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
} 