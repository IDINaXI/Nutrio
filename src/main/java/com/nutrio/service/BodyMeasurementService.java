package com.nutrio.service;

import com.nutrio.model.BodyMeasurement;
import com.nutrio.repository.BodyMeasurementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BodyMeasurementService {
    @Autowired
    private BodyMeasurementRepository bodyMeasurementRepository;

    public BodyMeasurement saveMeasurement(BodyMeasurement measurement) {
        return bodyMeasurementRepository.save(measurement);
    }

    public List<BodyMeasurement> getUserMeasurements(Long userId) {
        return bodyMeasurementRepository.findByUserIdOrderByDateAsc(userId);
    }

    public void deleteMeasurement(Long id) {
        bodyMeasurementRepository.deleteById(id);
    }
} 