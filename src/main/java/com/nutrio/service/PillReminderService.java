package com.nutrio.service;

import com.nutrio.model.PillReminder;
import com.nutrio.repository.PillReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PillReminderService {
    @Autowired
    private PillReminderRepository pillReminderRepository;

    public PillReminder saveReminder(PillReminder reminder) {
        return pillReminderRepository.save(reminder);
    }

    public List<PillReminder> getUserReminders(Long userId) {
        return pillReminderRepository.findByUserId(userId);
    }

    public void deleteReminder(Long id) {
        pillReminderRepository.deleteById(id);
    }
} 