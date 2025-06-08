package com.nutrio.service;

import com.nutrio.model.WeightEntry;
import com.nutrio.repository.WeightEntryRepository;
import com.nutrio.repository.UserRepository;
import com.nutrio.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WeightEntryService {
    
    @Autowired
    private WeightEntryRepository weightEntryRepository;

    @Autowired
    private UserRepository userRepository;

    public WeightEntry saveWeightEntry(WeightEntry weightEntry) {
        WeightEntry saved = weightEntryRepository.save(weightEntry);
        // Обновляем поле weight у пользователя
        User user = userRepository.findById(weightEntry.getUserId()).orElse(null);
        if (user != null) {
            user.setWeight(weightEntry.getWeight());
            userRepository.save(user);
        }
        return saved;
    }

    public List<WeightEntry> getUserWeightHistory(Long userId) {
        return weightEntryRepository.findByUserIdOrderByDateAsc(userId);
    }

    public WeightEntry getLatestWeightEntry(Long userId) {
        List<WeightEntry> entries = weightEntryRepository.findByUserIdOrderByDateAsc(userId);
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    public void deleteWeightEntry(Long id) {
        weightEntryRepository.deleteById(id);
    }
} 