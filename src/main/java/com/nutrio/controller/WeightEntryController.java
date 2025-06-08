package com.nutrio.controller;

import com.nutrio.model.WeightEntry;
import com.nutrio.service.WeightEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weight")
public class WeightEntryController {

    @Autowired
    private WeightEntryService weightEntryService;

    @PostMapping
    public ResponseEntity<WeightEntry> addWeightEntry(@RequestBody WeightEntry weightEntry) {
        return ResponseEntity.ok(weightEntryService.saveWeightEntry(weightEntry));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WeightEntry>> getUserWeightHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(weightEntryService.getUserWeightHistory(userId));
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<WeightEntry> getLatestWeightEntry(@PathVariable Long userId) {
        WeightEntry latest = weightEntryService.getLatestWeightEntry(userId);
        return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWeightEntry(@PathVariable Long id) {
        weightEntryService.deleteWeightEntry(id);
        return ResponseEntity.ok().build();
    }
} 