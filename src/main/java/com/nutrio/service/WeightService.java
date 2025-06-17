package com.nutrio.service;

import com.nutrio.model.WeightEntry;
import com.nutrio.repository.WeightEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class WeightService {
    
    @Autowired
    private WeightEntryRepository weightEntryRepository;

    public List<WeightEntry> getUserWeightHistory(Long userId) {
        List<WeightEntry> entries = weightEntryRepository.findByUserIdOrderByDateDesc(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ru", "RU"));
        entries.forEach(entry -> entry.setFormattedDate(entry.getDate().format(formatter)));
        return entries;
    }
} 