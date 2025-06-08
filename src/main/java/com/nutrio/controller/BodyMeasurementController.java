package com.nutrio.controller;

import com.nutrio.model.BodyMeasurement;
import com.nutrio.service.BodyMeasurementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/body-measurements")
public class BodyMeasurementController {
    @Autowired
    private BodyMeasurementService bodyMeasurementService;

    @PostMapping
    public ResponseEntity<BodyMeasurement> addMeasurement(@RequestBody BodyMeasurement measurement) {
        return ResponseEntity.ok(bodyMeasurementService.saveMeasurement(measurement));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BodyMeasurement>> getUserMeasurements(@PathVariable Long userId) {
        return ResponseEntity.ok(bodyMeasurementService.getUserMeasurements(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMeasurement(@PathVariable Long id) {
        bodyMeasurementService.deleteMeasurement(id);
        return ResponseEntity.ok().build();
    }
} 