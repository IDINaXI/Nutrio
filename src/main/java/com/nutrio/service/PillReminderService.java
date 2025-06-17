package com.nutrio.service;

import com.nutrio.model.PillReminder;
import com.nutrio.repository.PillReminderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PillReminderService {
    @Autowired
    private PillReminderRepository pillReminderRepository;

    public PillReminder saveReminder(PillReminder reminder) {
        return pillReminderRepository.save(reminder);
    }

    public List<PillReminder> getUserReminders(Long userId) {
        List<PillReminder> reminders = pillReminderRepository.findByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm", new Locale("ru", "RU"));
        reminders.forEach(reminder -> reminder.setFormattedTime(reminder.getTime().format(formatter)));
        return reminders;
    }

    public void deleteReminder(Long id) {
        pillReminderRepository.deleteById(id);
    }
} 