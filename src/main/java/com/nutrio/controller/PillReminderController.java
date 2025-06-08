package com.nutrio.controller;

import com.nutrio.model.PillReminder;
import com.nutrio.service.PillReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pill-reminders")
public class PillReminderController {
    @Autowired
    private PillReminderService pillReminderService;

    @PostMapping
    public ResponseEntity<PillReminder> addReminder(@RequestBody PillReminder reminder) {
        return ResponseEntity.ok(pillReminderService.saveReminder(reminder));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PillReminder>> getUserReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(pillReminderService.getUserReminders(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReminder(@PathVariable Long id) {
        pillReminderService.deleteReminder(id);
        return ResponseEntity.ok().build();
    }
} 