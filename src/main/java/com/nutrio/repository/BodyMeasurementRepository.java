package com.nutrio.repository;

import com.nutrio.model.BodyMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findByUserIdOrderByDateAsc(Long userId);
} 