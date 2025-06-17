package com.nutrio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrio.client.GeminiClient;
import com.nutrio.model.MealPlan;
import com.nutrio.model.Meal;
import com.nutrio.model.Meal.MealType;
import com.nutrio.model.User;
import com.nutrio.repository.MealPlanRepository;
import com.nutrio.repository.UserRepository;
import com.nutrio.model.DayMealPlan;
import com.nutrio.repository.DayMealPlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MealPlanService {
    private static final Logger logger = LoggerFactory.getLogger(MealPlanService.class);
    private final UserRepository userRepository;
    private final MealPlanRepository mealPlanRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final DayMealPlanRepository dayMealPlanRepository;
    private final AIService aiService;

    @Autowired
    public MealPlanService(
            UserRepository userRepository,
            MealPlanRepository mealPlanRepository,
            GeminiClient geminiClient,
            DayMealPlanRepository dayMealPlanRepository,
            AIService aiService) {
        this.userRepository = userRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.geminiClient = geminiClient;
        this.dayMealPlanRepository = dayMealPlanRepository;
        this.aiService = aiService;
        this.objectMapper = new ObjectMapper();
    }

    public MealPlan generateMealPlan(User user) {
        try {
            logger.info("Generating meal plan for user: {}, allergies: {}", 
                user.getName(), 
                user.getAllergies() != null ? user.getAllergies() : "none");
            
            String prompt = createPrompt(user);
            logger.info("Generated prompt for Gemini: {}", prompt);
            
            String aiResponse = geminiClient.generateMealPlan(prompt);
            logger.info("Received response from Gemini: {}", aiResponse);
            
            return parseAiResponse(aiResponse);
        } catch (Exception e) {
            logger.error("Error generating meal plan: {}", e.getMessage());
            throw new RuntimeException("Failed to generate meal plan", e);
        }
    }

    private String createPrompt(User user) {
        String prompt = String.format("""
            You are an AI nutritionist. Create a detailed meal plan for 7 days (week) in JSON format.
            %s

            User characteristics:
            - Age: %d
            - Gender: %s
            - Weight: %.1f kg
            - Height: %.1f cm
            - Activity Level: %s
            - Goal: %s

            Required JSON structure (return ONLY this structure):
            {
                "days": [
                    {
                        "date": "YYYY-MM-DD",
                        "breakfast": {
                            "name": "string",
                            "mealType": "BREAKFAST",
                            "calories": number,
                            "proteins": number,
                            "fats": number,
                            "carbohydrates": number,
                            "ingredients": ["string"],
                            "recipe": "string"
                        },
                        "lunch": { ... },
                        "dinner": { ... },
                        "snack": { ... },
                        "totalCalories": number,
                        "macronutrients": {
                            "proteins": number,
                            "fats": number,
                            "carbs": number
                        }
                    }
                ]
            }

            Rules:
            1. Return ONLY the JSON object, without additional text
            2. All calorie and macronutrient values must be realistic numbers
            3. Total daily calories should match user's needs
            4. Ensure all JSON fields are present and properly formatted
            5. Meals should not repeat during the week
            6. Each meal must include ingredients and basic recipe
            """,
            user.getAllergies() != null && !user.getAllergies().isEmpty() 
                ? String.format("""
                    WARNING! User has allergies: %s
                    STRICTLY FORBIDDEN to include any meals containing these allergens!
                    Each meal must be safe for the user.
                    """, String.join(", ", user.getAllergies()))
                : "",
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal()
        );
        
        logger.info("Full week prompt (length: {}): {}", prompt.length(), prompt);
        return prompt;
    }

    private MealPlan parseAiResponse(String response) {
        try {
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);
            if (!matcher.find()) {
                throw new RuntimeException("Failed to extract JSON from AI response");
            }
            String json = matcher.group();
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            MealPlan mealPlan = new MealPlan();
            List<Map<String, Object>> days = (List<Map<String, Object>>) data.get("days");
            if (days == null || days.size() != 7) {
                throw new RuntimeException("AI did not return 7 days in the plan");
            }
            // Преобразуем days в List<MealPlan.WeeklyDayPlan>
            List<MealPlan.WeeklyDayPlan> week = new ArrayList<>();
            for (Map<String, Object> day : days) {
                MealPlan.WeeklyDayPlan wdp = new MealPlan.WeeklyDayPlan();
                wdp.setDate((String) day.get("date"));
                wdp.setBreakfast(objectMapper.convertValue(day.get("breakfast"), Meal.class));
                wdp.setLunch(objectMapper.convertValue(day.get("lunch"), Meal.class));
                wdp.setDinner(objectMapper.convertValue(day.get("dinner"), Meal.class));
                wdp.setSnack(objectMapper.convertValue(day.get("snack"), Meal.class));
                wdp.setTotalCalories(((Number) day.get("totalCalories")).intValue());
                wdp.setMacronutrients(objectMapper.convertValue(day.get("macronutrients"), MealPlan.Macronutrients.class));
                week.add(wdp);
            }
            mealPlan.setWeek(week);
            return mealPlan;
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            throw new RuntimeException("Error parsing AI response: " + e.getMessage(), e);
        }
    }

    public List<MealPlan> getUserMealPlans(Long userId) {
        return mealPlanRepository.findByUserId(userId);
    }

    public DayMealPlan generateDayMealPlan(User user) {
        // Валидация профиля пользователя
        if (user.getAge() <= 0 ||
            user.getGender() == null ||
            user.getWeight() <= 0.0 ||
            user.getHeight() <= 0.0 ||
            user.getActivityLevel() == null ||
            user.getGoal() == null) {
            throw new RuntimeException("Пожалуйста, заполните профиль полностью для генерации плана питания.");
        }

        logger.info("Generating day meal plan for user: {}, allergies: {}", 
            user.getName(), 
            user.getAllergies() != null ? user.getAllergies() : "none");

        String prompt = createDayPrompt(user);
        logger.info("Generated day prompt for Gemini: {}", prompt);
        String aiResponse = geminiClient.generateMealPlan(prompt);
        logger.info("Received day response from Gemini: {}", aiResponse);
        DayMealPlan plan = parseDayAiResponse(aiResponse, user);
        return dayMealPlanRepository.save(plan);
    }

    private String createDayPrompt(User user) {
        String prompt = String.format("""
            You are an AI nutritionist. Create a detailed meal plan for one day for the user.
            %s

            User characteristics:
            Name: %s
            Age: %d
            Gender: %s
            Weight: %.1f kg
            Height: %.1f cm
            Activity Level: %s
            Goal: %s

            IMPORTANT: Return ONLY JSON, without explanations, markdown, or other symbols. No comments, just JSON!

            Example structure:
            {
                "breakfast": {
                    "name": "string",
                    "mealType": "BREAKFAST",
                    "calories": number,
                    "proteins": number,
                    "fats": number,
                    "carbohydrates": number,
                    "ingredients": ["string"],
                    "recipe": "string"
                },
                "lunch": { ... },
                "dinner": { ... },
                "snack": { ... },
                "totalCalories": number,
                "macronutrients": {
                    "proteins": number,
                    "fats": number,
                    "carbohydrates": number
                }
            }
            """,
            user.getAllergies() != null && !user.getAllergies().isEmpty() 
                ? String.format("""
                    WARNING! User has allergies: %s
                    STRICTLY FORBIDDEN to include any meals containing these allergens!
                    Each meal must be safe for the user.
                    """, String.join(", ", user.getAllergies()))
                : "",
            user.getName(),
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal()
        );
        
        logger.info("Full day prompt (length: {}): {}", prompt.length(), prompt);
        return prompt;
    }

    private DayMealPlan parseDayAiResponse(String response, User user) {
        try {
            logger.info("AI raw response: {}", response); // Логируем ответ
            // Убираем markdown, если есть
            response = response.replaceAll("```json", "").replaceAll("```", "").trim();
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);
            if (!matcher.find()) {
                throw new RuntimeException("Failed to extract JSON from AI response");
            }
            String json = matcher.group();
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            DayMealPlan plan = new DayMealPlan();
            plan.setBreakfast(objectMapper.convertValue(data.get("breakfast"), Meal.class));
            plan.setLunch(objectMapper.convertValue(data.get("lunch"), Meal.class));
            plan.setDinner(objectMapper.convertValue(data.get("dinner"), Meal.class));
            plan.setSnack(objectMapper.convertValue(data.get("snack"), Meal.class));
            plan.setTotalCalories(((Number) data.get("totalCalories")).intValue());
            plan.setMacronutrients(objectMapper.convertValue(data.get("macronutrients"), MealPlan.Macronutrients.class));
            plan.setUser(user);
            return plan;
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            throw new RuntimeException("Error parsing AI response: " + e.getMessage(), e);
        }
    }

    public List<DayMealPlan> getUserDayMealPlans(Long userId) {
        return dayMealPlanRepository.findByUserId(userId);
    }

    public MealPlan.WeeklyDayPlan regenerateDayMealPlan(User user, String day, MealPlan currentMealPlan) {
        return aiService.regenerateDayMealPlan(user, day, currentMealPlan);
    }
}