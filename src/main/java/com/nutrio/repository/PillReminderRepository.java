package com.nutrio.repository;

import com.nutrio.model.PillReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface PillReminderRepository extends JpaRepository<PillReminder, Long> {
    List<PillReminder> findByUserId(Long userId);
} 