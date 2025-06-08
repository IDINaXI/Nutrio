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
        return String.format(
            "You are a nutrition AI assistant. Generate a personalized meal plan for 7 days (a week) in JSON format. " +
            "Each day must include: breakfast, lunch, dinner, snack, totalCalories, and macronutrients (proteins, fats, carbs). " +
            "All fields must be filled. Meals should not repeat within the week. Respond ONLY with a valid JSON object, no explanations.\n" +
            "User characteristics:\n" +
            "- Age: %d\n" +
            "- Gender: %s\n" +
            "- Weight: %.1f kg\n" +
            "- Height: %.1f cm\n" +
            "- Activity Level: %s\n" +
            "- Goal: %s\n\n" +
            "Required JSON structure (respond with ONLY this structure):\n" +
            "{\n" +
            "  \"days\": [\n" +
            "    {\n" +
            "      \"date\": \"YYYY-MM-DD\",\n" +
            "      \"breakfast\": { ... },\n" +
            "      \"lunch\": { ... },\n" +
            "      \"dinner\": { ... },\n" +
            "      \"snack\": { ... },\n" +
            "      \"totalCalories\": number,\n" +
            "      \"macronutrients\": {\n" +
            "        \"proteins\": number,\n" +
            "        \"fats\": number,\n" +
            "        \"carbs\": number\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}\n" +
            "Rules:\n" +
            "1. Respond with ONLY the JSON object, no other text\n" +
            "2. All nutritional values must be realistic numbers\n" +
            "3. Total daily calories should match user's needs based on their characteristics\n" +
            "4. Ensure all JSON fields are present and properly formatted\n" +
            "5. Meals should not repeat within the week\n" +
            "6. Each meal must include ingredients and a basic recipe\n",
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal()
        );
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
        String prompt = createDayPrompt(user);
        logger.info("Generated day prompt for Gemini: {}", prompt);
        String aiResponse = geminiClient.generateMealPlan(prompt);
        logger.info("Received day response from Gemini: {}", aiResponse);
        DayMealPlan plan = parseDayAiResponse(aiResponse, user);
        return dayMealPlanRepository.save(plan);
    }

    private String createDayPrompt(User user) {
        return String.format("""
            Ты — ИИ-диетолог. Составь подробный план питания на один день для пользователя:
            Имя: %s
            Возраст: %d
            Пол: %s
            Вес: %.1f кг
            Рост: %.1f см
            Уровень активности: %s
            Цель: %s

            ВАЖНО: Верни только JSON, без пояснений, markdown и других символов. Никаких комментариев, только JSON!

            Пример структуры:
            {
                "breakfast": { ... },
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
            user.getName(),
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal()
        );
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