package com.nutrio.controller;


import com.nutrio.model.WeightEntry;
import com.nutrio.repository.WeightEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/weights")
public class WeightController {

    @Autowired
    private WeightEntryRepository repo;

    @GetMapping
    public List<WeightEntry> getWeights(@PathVariable Long userId) {
        return repo.findByUserIdOrderByDateAsc(userId);
    }

    @PostMapping
    public WeightEntry addWeight(@PathVariable Long userId, @RequestBody WeightEntry entry) {
        entry.setUserId(userId);
        entry.setDate(LocalDate.now());
        return repo.save(entry);
    }
}