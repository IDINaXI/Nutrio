package com.nutrio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonParser;
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
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;

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
        this.objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        this.objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        this.objectMapper.getFactory().setCharacterEscapes(null);
    }

    public MealPlan generateMealPlan(User user) {
        try {
            logger.info("Generating meal plan for user: {}", user.getName());
            if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
                logger.info("User allergies: {}", String.join(", ", user.getAllergies()));
            } else {
                logger.info("User has no allergies");
            }
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
        String allergiesInfo = "";
        if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
            allergiesInfo = String.format("- Allergii: %s\nVAGNO: STROGO IZBEGAI ispolzovaniya lyubih ingredientov, na kotorie u polzovatelya allergiya!\n", 
                String.join(", ", user.getAllergies()));
            logger.info("Adding allergies to prompt: {}", allergiesInfo);
        }

        return String.format(
            "Ti - II-dietolog. Sostav podrobniy plan pitaniya na 7 dney (nedelyu) v formate JSON. " +
            "Kazhdyi den dolzhen vklyuchat: zavtrak, obed, uzhin, perekus, obshie kalorii i makronutrienty (belki, zhiri, uglevody). " +
            "Vse polya dolzhny byt zapolneny. Blyuda ne dolzhny povtoryatsya v techenie nedeli. Verni TOLKO validnyi JSON object, bez poyasnenii.\n" +
            "Harakteristiki polzovatelya:\n" +
            "- Vozrast: %d\n" +
            "- Pol: %s\n" +
            "- Ves: %.1f kg\n" +
            "- Rost: %.1f cm\n" +
            "- Uroven aktivnosti: %s\n" +
            "- Cel: %s\n" +
            "%s\n" +
            "Trebuemaya struktura JSON (verni TOLKO etu strukturu):\n" +
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
            "Pravila:\n" +
            "1. Verni TOLKO JSON object, bez dopolnitelnogo teksta\n" +
            "2. Vse znacheniya kalorii i makronutrientov dolzhny byt realisticnymi chislami\n" +
            "3. Obshie kalorii za den dolzhny sootvetstvovat potrebnostyam polzovatelya\n" +
            "4. Ubedis, chto vse polya JSON prisutstvuyut i pravilno otformatirovany\n" +
            "5. Blyuda ne dolzhny povtoryatsya v techenie nedeli\n" +
            "6. Kazhdoe blyudo dolzhno vklyuchat ingredienty i bazovyi recept\n" +
            "%s",
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal(),
            allergiesInfo,
            user.getAllergies() != null && !user.getAllergies().isEmpty() 
                ? "7. STROGO IZBEGAI ispolzovaniya lyubih ingredientov, na kotorie u polzovatelya allergiya!\n"
                : ""
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
            // Удаляем markdown, если есть
            json = json.replaceAll("```json", "").replaceAll("```", "").trim();
            
            // Декодируем Unicode escape sequences и обрабатываем русские символы
            json = StringEscapeUtils.unescapeJava(json);
            
            // Конвертируем в байты и обратно для правильной обработки кодировки
            byte[] bytes = json.getBytes(StandardCharsets.ISO_8859_1);
            json = new String(bytes, StandardCharsets.UTF_8);
            
            // Дополнительная обработка для корректного отображения русских символов
            json = json.replaceAll("\\\\u([0-9a-fA-F]{4})", "\\\\u$1");
            json = StringEscapeUtils.unescapeJava(json);
            
            logger.info("Processed JSON before parsing: {}", json);
            
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
                
                // Проверяем каждое блюдо на наличие аллергенов
                Meal breakfast = objectMapper.convertValue(day.get("breakfast"), Meal.class);
                Meal lunch = objectMapper.convertValue(day.get("lunch"), Meal.class);
                Meal dinner = objectMapper.convertValue(day.get("dinner"), Meal.class);
                Meal snack = objectMapper.convertValue(day.get("snack"), Meal.class);
                
                logger.info("Checking meals for allergens:");
                logger.info("Breakfast ingredients: {}", breakfast.getIngredients());
                logger.info("Lunch ingredients: {}", lunch.getIngredients());
                logger.info("Dinner ingredients: {}", dinner.getIngredients());
                logger.info("Snack ingredients: {}", snack.getIngredients());
                
                wdp.setBreakfast(breakfast);
                wdp.setLunch(lunch);
                wdp.setDinner(dinner);
                wdp.setSnack(snack);
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

        logger.info("Generating day meal plan for user: {}", user.getName());
        if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
            logger.info("User allergies: {}", String.join(", ", user.getAllergies()));
        } else {
            logger.info("User has no allergies");
        }

        String prompt = createDayPrompt(user);
        logger.info("Generated day prompt for Gemini: {}", prompt);
        String aiResponse = geminiClient.generateMealPlan(prompt);
        logger.info("Received day response from Gemini: {}", aiResponse);
        DayMealPlan plan = parseDayAiResponse(aiResponse, user);
        return dayMealPlanRepository.save(plan);
    }

    private String createDayPrompt(User user) {
        String allergiesInfo = "";
        if (user.getAllergies() != null && !user.getAllergies().isEmpty()) {
            allergiesInfo = String.format("Allergii: %s\nVAGNO: STROGO IZBEGAI ispolzovaniya lyubih ingredientov, na kotorie u polzovatelya allergiya!\n", 
                String.join(", ", user.getAllergies()));
            logger.info("Adding allergies to day prompt: {}", allergiesInfo);
        }

        return String.format("""
            Ti - II-dietolog. Sostav podrobniy plan pitaniya na odin den dlya polzovatelya:
            Imya: %s
            Vozrast: %d
            Pol: %s
            Ves: %.1f kg
            Rost: %.1f cm
            Uroven aktivnosti: %s
            Cel: %s
            %s

            VAGNO: Verni tolko JSON, bez poyasnenii, markdown i drugih simvolov. Nikakih kommentariev, tolko JSON!
            %s

            Primer struktury:
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
            user.getGoal(),
            allergiesInfo,
            user.getAllergies() != null && !user.getAllergies().isEmpty() 
                ? "VAGNO: STROGO IZBEGAI ispolzovaniya lyubih ingredientov, na kotorie u polzovatelya allergiya!"
                : ""
        );
    }

    private DayMealPlan parseDayAiResponse(String response, User user) {
        try {
            Pattern pattern = Pattern.compile("\\{.*\\}", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(response);
            if (!matcher.find()) {
                throw new RuntimeException("Failed to extract JSON from AI response");
            }
            String json = matcher.group();
            // Удаляем markdown, если есть
            json = json.replaceAll("```json", "").replaceAll("```", "").trim();
            
            // Декодируем Unicode escape sequences и обрабатываем русские символы
            json = StringEscapeUtils.unescapeJava(json);
            
            // Конвертируем в байты и обратно для правильной обработки кодировки
            byte[] bytes = json.getBytes(StandardCharsets.ISO_8859_1);
            json = new String(bytes, StandardCharsets.UTF_8);
            
            // Дополнительная обработка для корректного отображения русских символов
            json = json.replaceAll("\\\\u([0-9a-fA-F]{4})", "\\\\u$1");
            json = StringEscapeUtils.unescapeJava(json);
            
            logger.info("Processed JSON before parsing: {}", json);
            
            Map<String, Object> data = objectMapper.readValue(json, Map.class);
            DayMealPlan plan = new DayMealPlan();

            // Проверяем каждое блюдо на наличие аллергенов
            Meal breakfast = objectMapper.convertValue(data.get("breakfast"), Meal.class);
            Meal lunch = objectMapper.convertValue(data.get("lunch"), Meal.class);
            Meal dinner = objectMapper.convertValue(data.get("dinner"), Meal.class);
            Meal snack = objectMapper.convertValue(data.get("snack"), Meal.class);

            logger.info("Checking meals for allergens:");
            logger.info("Breakfast ingredients: {}", breakfast.getIngredients());
            logger.info("Lunch ingredients: {}", lunch.getIngredients());
            logger.info("Dinner ingredients: {}", dinner.getIngredients());
            logger.info("Snack ingredients: {}", snack.getIngredients());

            plan.setBreakfast(breakfast);
            plan.setLunch(lunch);
            plan.setDinner(dinner);
            plan.setSnack(snack);
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