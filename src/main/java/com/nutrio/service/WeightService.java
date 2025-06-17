package com.nutrio.service;

import com.nutrio.model.WeightEntry;
import com.nutrio.repository.WeightEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeightService {
    
    @Autowired
    private WeightEntryRepository weightEntryRepository;

    public List<WeightEntry> getUserWeightHistory(Long userId) {
        return weightEntryRepository.findByUserIdOrderByDateDesc(userId);
    }
} 