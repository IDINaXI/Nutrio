package com.nutrio.controller;

import com.nutrio.model.MealPlan;
import com.nutrio.model.User;
import com.nutrio.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(
    originPatterns = {
        "https://nutrio-production.up.railway.app",
        "http://127.0.0.1:[*]"
    },
    allowedHeaders = {"Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"},
    allowCredentials = "true"
)
public class AIController {
    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    private final AIService aiService;

    @Autowired
    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping(value = "/generate", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> generateMealPlan(@RequestBody User user) {
        try {
            logger.info("Получен запрос на генерацию плана питания для пользователя: {}", user.getName());
            MealPlan mealPlan = aiService.generateMealPlan(user);
            logger.info("План питания успешно сгенерирован");
            return ResponseEntity.ok(mealPlan);
        } catch (Exception e) {
            logger.error("Ошибка при генерации плана питания: {}", e.getMessage());
            if (e.getMessage().contains("Insufficient credits")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Сервис генерации плана питания временно недоступен. Пожалуйста, попробуйте позже.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Произошла ошибка при генерации плана питания. Пожалуйста, попробуйте позже.");
        }
    }
} 