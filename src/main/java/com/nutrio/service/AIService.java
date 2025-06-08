package com.nutrio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nutrio.client.GeminiClient;
import com.nutrio.model.MealPlan;
import com.nutrio.model.User;
import com.nutrio.model.Meal;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public AIService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
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
            logger.warn("Failed to generate meal plan using AI service, falling back to basic meal plan: {}", e.getMessage());
            return generateFallbackMealPlan(user);
        }
    }

    private MealPlan generateFallbackMealPlan(User user) {
        Map<String, Double> macros = calculateMacros(user);
        double totalCalories = calculateTotalCalories(user);

        MealPlan mealPlan = new MealPlan();
        mealPlan.setBreakfast(generateBreakfast(user, macros));
        mealPlan.setLunch(generateLunch(user, macros));
        mealPlan.setDinner(generateDinner(user, macros));
        mealPlan.setSnacks(generateSnacks(user, macros));
        mealPlan.setTotalCalories((int) totalCalories);

        MealPlan.Macronutrients macronutrients = new MealPlan.Macronutrients();
        macronutrients.setProteins(macros.get("protein"));
        macronutrients.setFats(macros.get("fat"));
        macronutrients.setCarbs(macros.get("carbs"));
        mealPlan.setMacronutrients(macronutrients);

        return mealPlan;
    }

    private List<Meal> generateBreakfast(User user, Map<String, Double> macros) {
        List<Meal> breakfasts = new ArrayList<>();
        
        // High protein breakfast
        Meal proteinOatmeal = new Meal();
        proteinOatmeal.setName("Протеиновая овсянка");
        proteinOatmeal.setMealType(Meal.MealType.BREAKFAST);
        proteinOatmeal.setCalories(450);
        proteinOatmeal.setProteins(macros.get("protein") * 0.2);
        proteinOatmeal.setFats(macros.get("fat") * 0.15);
        proteinOatmeal.setCarbohydrates(macros.get("carbs") * 0.25);
        proteinOatmeal.setIngredients(Arrays.asList("Овсяные хлопья", "Протеиновый порошок", "Банан", "Миндальное молоко", "Семена чиа"));
        proteinOatmeal.setRecipe("Сварите овсянку на миндальном молоке, добавьте протеиновый порошок, украсьте бананом и семенами чиа");
        breakfasts.add(proteinOatmeal);

        // Balanced breakfast
        Meal avocadoToast = new Meal();
        avocadoToast.setName("Тост с авокадо и яйцами");
        avocadoToast.setMealType(Meal.MealType.BREAKFAST);
        avocadoToast.setCalories(500);
        avocadoToast.setProteins(macros.get("protein") * 0.25);
        avocadoToast.setFats(macros.get("fat") * 0.2);
        avocadoToast.setCarbohydrates(macros.get("carbs") * 0.2);
        avocadoToast.setIngredients(Arrays.asList("Цельнозерновой хлеб", "Авокадо", "Яйца", "Шпинат", "Помидоры черри"));
        avocadoToast.setRecipe("Поджарьте хлеб, разомните авокадо, добавьте пашот яйца и овощи");
        breakfasts.add(avocadoToast);

        // Quick breakfast
        Meal yogurtParfait = new Meal();
        yogurtParfait.setName("Греческий йогурт с гранолой");
        yogurtParfait.setMealType(Meal.MealType.BREAKFAST);
        yogurtParfait.setCalories(400);
        yogurtParfait.setProteins(macros.get("protein") * 0.3);
        yogurtParfait.setFats(macros.get("fat") * 0.1);
        yogurtParfait.setCarbohydrates(macros.get("carbs") * 0.15);
        yogurtParfait.setIngredients(Arrays.asList("Греческий йогурт", "Смесь ягод", "Гранола", "Мед"));
        yogurtParfait.setRecipe("Выложите слоями йогурт, ягоды и гранолу, полейте медом");
        breakfasts.add(yogurtParfait);

        return breakfasts;
    }

    private List<Meal> generateLunch(User user, Map<String, Double> macros) {
        List<Meal> lunches = new ArrayList<>();
        
        // Protein-rich lunch
        Meal chickenSalad = new Meal();
        chickenSalad.setName("Салат с куриной грудкой");
        chickenSalad.setMealType(Meal.MealType.LUNCH);
        chickenSalad.setCalories(550);
        chickenSalad.setProteins(macros.get("protein") * 0.3);
        chickenSalad.setFats(macros.get("fat") * 0.15);
        chickenSalad.setCarbohydrates(macros.get("carbs") * 0.2);
        chickenSalad.setIngredients(Arrays.asList("Куриная грудка", "Смесь салатов", "Киноа", "Оливковое масло", "Бальзамический уксус"));
        chickenSalad.setRecipe("Обжарьте куриную грудку, подавайте на смеси салатов с киноа, заправьте оливковым маслом и бальзамическим уксусом");
        lunches.add(chickenSalad);

        // Balanced lunch
        Meal mediterraneanBowl = new Meal();
        mediterraneanBowl.setName("Средиземноморская чаша");
        mediterraneanBowl.setMealType(Meal.MealType.LUNCH);
        mediterraneanBowl.setCalories(600);
        mediterraneanBowl.setProteins(macros.get("protein") * 0.25);
        mediterraneanBowl.setFats(macros.get("fat") * 0.2);
        mediterraneanBowl.setCarbohydrates(macros.get("carbs") * 0.25);
        mediterraneanBowl.setIngredients(Arrays.asList("Лосось", "Бурый рис", "Запеченные овощи", "Хумус", "Сыр фета"));
        mediterraneanBowl.setRecipe("Подайте запеченного лосося с бурым рисом, запеченными овощами, хумусом и сыром фета");
        lunches.add(mediterraneanBowl);

        // Vegetarian lunch
        Meal lentilBowl = new Meal();
        lentilBowl.setName("Будда-боул с чечевицей");
        lentilBowl.setMealType(Meal.MealType.LUNCH);
        lentilBowl.setCalories(500);
        lentilBowl.setProteins(macros.get("protein") * 0.2);
        lentilBowl.setFats(macros.get("fat") * 0.15);
        lentilBowl.setCarbohydrates(macros.get("carbs") * 0.3);
        lentilBowl.setIngredients(Arrays.asList("Чечевица", "Батат", "Кейл", "Тахин", "Тыквенные семечки"));
        lentilBowl.setRecipe("Смешайте отварную чечевицу с запеченным бататом и кейлом, полейте соусом тахини и посыпьте тыквенными семечками");
        lunches.add(lentilBowl);

        return lunches;
    }

    private List<Meal> generateDinner(User user, Map<String, Double> macros) {
        List<Meal> dinners = new ArrayList<>();
        
        // High protein dinner
        Meal steakDinner = new Meal();
        steakDinner.setName("Стейк с овощами");
        steakDinner.setMealType(Meal.MealType.DINNER);
        steakDinner.setCalories(650);
        steakDinner.setProteins(macros.get("protein") * 0.35);
        steakDinner.setFats(macros.get("fat") * 0.25);
        steakDinner.setCarbohydrates(macros.get("carbs") * 0.15);
        steakDinner.setIngredients(Arrays.asList("Стейк", "Брокколи", "Батат", "Чеснок", "Травы"));
        steakDinner.setRecipe("Обжарьте стейк до желаемой прожарки, подавайте с запеченными овощами и бататом");
        dinners.add(steakDinner);

        // Balanced dinner
        Meal salmonDinner = new Meal();
        salmonDinner.setName("Запеченный лосось с киноа");
        salmonDinner.setMealType(Meal.MealType.DINNER);
        salmonDinner.setCalories(600);
        salmonDinner.setProteins(macros.get("protein") * 0.3);
        salmonDinner.setFats(macros.get("fat") * 0.2);
        salmonDinner.setCarbohydrates(macros.get("carbs") * 0.2);
        salmonDinner.setIngredients(Arrays.asList("Лосось", "Киноа", "Спаржа", "Лимон", "Укроп"));
        salmonDinner.setRecipe("Запеките лосося с лимоном и укропом, подавайте с киноа и запеченной спаржей");
        dinners.add(salmonDinner);

        // Light dinner
        Meal turkeyStirFry = new Meal();
        turkeyStirFry.setName("Стир-фрай с индейкой");
        turkeyStirFry.setMealType(Meal.MealType.DINNER);
        turkeyStirFry.setCalories(550);
        turkeyStirFry.setProteins(macros.get("protein") * 0.25);
        turkeyStirFry.setFats(macros.get("fat") * 0.15);
        turkeyStirFry.setCarbohydrates(macros.get("carbs") * 0.25);
        turkeyStirFry.setIngredients(Arrays.asList("Фарш из индейки", "Бурый рис", "Смесь овощей", "Соевый соус", "Имбирь"));
        turkeyStirFry.setRecipe("Обжарьте фарш с овощами, подавайте с бурым рисом и соусом из соевого соуса и имбиря");
        dinners.add(turkeyStirFry);

        return dinners;
    }

    private List<Meal> generateSnacks(User user, Map<String, Double> macros) {
        List<Meal> snacks = new ArrayList<>();
        
        // Protein snack
        Meal proteinSmoothie = new Meal();
        proteinSmoothie.setName("Протеиновый смузи");
        proteinSmoothie.setMealType(Meal.MealType.SNACK);
        proteinSmoothie.setCalories(300);
        proteinSmoothie.setProteins(macros.get("protein") * 0.15);
        proteinSmoothie.setFats(macros.get("fat") * 0.1);
        proteinSmoothie.setCarbohydrates(macros.get("carbs") * 0.1);
        proteinSmoothie.setIngredients(Arrays.asList("Протеиновый порошок", "Миндальное молоко", "Замороженные ягоды", "Шпинат"));
        proteinSmoothie.setRecipe("Смешайте все ингредиенты в блендере до однородной массы");
        snacks.add(proteinSmoothie);

        // Balanced snack
        Meal appleSnack = new Meal();
        appleSnack.setName("Яблоко с ореховой пастой");
        appleSnack.setMealType(Meal.MealType.SNACK);
        appleSnack.setCalories(250);
        appleSnack.setProteins(macros.get("protein") * 0.1);
        appleSnack.setFats(macros.get("fat") * 0.15);
        appleSnack.setCarbohydrates(macros.get("carbs") * 0.15);
        appleSnack.setIngredients(Arrays.asList("Яблоко", "Миндальная паста", "Корица"));
        appleSnack.setRecipe("Нарежьте яблоко и подавайте с миндальной пастой, посыпьте корицей");
        snacks.add(appleSnack);

        // Quick snack
        Meal yogurtSnack = new Meal();
        yogurtSnack.setName("Греческий йогурт с орехами");
        yogurtSnack.setMealType(Meal.MealType.SNACK);
        yogurtSnack.setCalories(200);
        yogurtSnack.setProteins(macros.get("protein") * 0.2);
        yogurtSnack.setFats(macros.get("fat") * 0.1);
        yogurtSnack.setCarbohydrates(macros.get("carbs") * 0.05);
        yogurtSnack.setIngredients(Arrays.asList("Греческий йогурт", "Смесь орехов", "Мед"));
        yogurtSnack.setRecipe("Подайте йогурт с орехами и полейте медом");
        snacks.add(yogurtSnack);

        return snacks;
    }

    private Map<String, Double> calculateMacros(User user) {
        double weight = user.getWeight();
        double height = user.getHeight();
        double age = user.getAge();
        double activityMultiplier = getActivityMultiplier(user.getActivityLevel());
        double goalMultiplier = getGoalMultiplier(user.getGoal());

        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        double tdee = bmr * activityMultiplier * goalMultiplier;

        // Calculate macros based on goals
        double protein = weight * 2.2; // 2.2g per kg of body weight
        double fat = (tdee * 0.25) / 9; // 25% of calories from fat
        double carbs = (tdee - (protein * 4) - (fat * 9)) / 4; // Remaining calories from carbs

        Map<String, Double> macros = new HashMap<>();
        macros.put("protein", protein);
        macros.put("fat", fat);
        macros.put("carbs", carbs);
        return macros;
    }

    private double calculateTotalCalories(User user) {
        double weight = user.getWeight();
        double height = user.getHeight();
        double age = user.getAge();
        double activityMultiplier = getActivityMultiplier(user.getActivityLevel());
        double goalMultiplier = getGoalMultiplier(user.getGoal());

        // Calculate BMR using Mifflin-St Jeor Equation
        double bmr = 10 * weight + 6.25 * height - 5 * age + 5;
        return bmr * activityMultiplier * goalMultiplier;
    }

    private double getActivityMultiplier(User.ActivityLevel activityLevel) {
        return switch (activityLevel) {
            case SEDENTARY -> 1.2;
            case LIGHTLY_ACTIVE -> 1.375;
            case MODERATELY_ACTIVE -> 1.55;
            case VERY_ACTIVE -> 1.725;
            case EXTREMELY_ACTIVE -> 1.9;
            default -> 1.2;
        };
    }

    private double getGoalMultiplier(User.Goal goal) {
        return switch (goal) {
            case LOSE_WEIGHT -> 0.85;
            case MAINTAIN_WEIGHT -> 1.0;
            case GAIN_WEIGHT -> 1.15;
            default -> 1.0;
        };
    }

    private MealPlan parseAiResponse(String response) {
        try {
            String content = null;
            // Try to parse the response as JSON (OpenRouter format)
            if (response.trim().startsWith("{")) {
                JsonNode root = objectMapper.readTree(response);
                content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
            } else {
                // Fallback: response is not JSON, treat as raw content
                content = response;
            }

            logger.info("Extracted content from response: {}", content);

            // Remove markdown code block markers if present
            content = content.trim();
            if (content.startsWith("```")) {
                int firstNewline = content.indexOf('\n');
                if (firstNewline != -1) {
                    content = content.substring(firstNewline + 1);
                } else {
                    content = content.replaceFirst("^```[a-zA-Z]*", "");
                }
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.lastIndexOf("```"));
            }
            content = content.trim();
            if (!content.startsWith("{")) {
                int startIndex = content.indexOf("{");
                if (startIndex == -1) {
                    throw new RuntimeException("No JSON object found in response");
                }
                content = content.substring(startIndex);
            }

            // Now parse the content as our meal plan JSON
            Map<String, Object> data = objectMapper.readValue(content, Map.class);
            MealPlan mealPlan = new MealPlan();

            // Parse weekly plan
            List<Map<String, Object>> weekData = (List<Map<String, Object>>) data.get("week");
            if (weekData != null) {
                List<MealPlan.WeeklyDayPlan> week = new ArrayList<>();
                for (Map<String, Object> dayData : weekData) {
                    MealPlan.WeeklyDayPlan dayPlan = new MealPlan.WeeklyDayPlan();
                    dayPlan.setDate((String) dayData.get("day"));
                    
                    // Parse meals with null checks
                    Map<String, Object> breakfastData = (Map<String, Object>) dayData.get("breakfast");
                    if (breakfastData != null) {
                        // Remove type field before conversion to avoid Jackson error
                        breakfastData.remove("type");
                        // Map 'carbs' to 'carbohydrates' if present
                        if (breakfastData.containsKey("carbs")) {
                            breakfastData.put("carbohydrates", breakfastData.get("carbs"));
                            breakfastData.remove("carbs");
                        }
                        Meal breakfast = objectMapper.convertValue(breakfastData, Meal.class);
                        breakfast.setMealType(Meal.MealType.BREAKFAST);
                        breakfast.setDescription((String) breakfastData.get("description"));
                        breakfast.setRecipe((String) breakfastData.get("recipe"));
                        breakfast.setImageUrl((String) breakfastData.get("imageUrl"));
                        dayPlan.setBreakfast(breakfast);
                    }
                    
                    Map<String, Object> lunchData = (Map<String, Object>) dayData.get("lunch");
                    if (lunchData != null) {
                        // Remove type field before conversion to avoid Jackson error
                        lunchData.remove("type");
                        // Map 'carbs' to 'carbohydrates' if present
                        if (lunchData.containsKey("carbs")) {
                            lunchData.put("carbohydrates", lunchData.get("carbs"));
                            lunchData.remove("carbs");
                        }
                        Meal lunch = objectMapper.convertValue(lunchData, Meal.class);
                        lunch.setMealType(Meal.MealType.LUNCH);
                        lunch.setDescription((String) lunchData.get("description"));
                        lunch.setRecipe((String) lunchData.get("recipe"));
                        lunch.setImageUrl((String) lunchData.get("imageUrl"));
                        dayPlan.setLunch(lunch);
                    }
                    
                    Map<String, Object> dinnerData = (Map<String, Object>) dayData.get("dinner");
                    if (dinnerData != null) {
                        // Remove type field before conversion to avoid Jackson error
                        dinnerData.remove("type");
                        // Map 'carbs' to 'carbohydrates' if present
                        if (dinnerData.containsKey("carbs")) {
                            dinnerData.put("carbohydrates", dinnerData.get("carbs"));
                            dinnerData.remove("carbs");
                        }
                        Meal dinner = objectMapper.convertValue(dinnerData, Meal.class);
                        dinner.setMealType(Meal.MealType.DINNER);
                        dinner.setDescription((String) dinnerData.get("description"));
                        dinner.setRecipe((String) dinnerData.get("recipe"));
                        dinner.setImageUrl((String) dinnerData.get("imageUrl"));
                        dayPlan.setDinner(dinner);
                    }
                    
                    Map<String, Object> snackData = (Map<String, Object>) dayData.get("snack");
                    if (snackData != null) {
                        // Remove type field before conversion to avoid Jackson error
                        snackData.remove("type");
                        // Map 'carbs' to 'carbohydrates' if present
                        if (snackData.containsKey("carbs")) {
                            snackData.put("carbohydrates", snackData.get("carbs"));
                            snackData.remove("carbs");
                        }
                        Meal snack = objectMapper.convertValue(snackData, Meal.class);
                        snack.setMealType(Meal.MealType.SNACK);
                        snack.setDescription((String) snackData.get("description"));
                        snack.setRecipe((String) snackData.get("recipe"));
                        snack.setImageUrl((String) snackData.get("imageUrl"));
                        dayPlan.setSnack(snack);
                    }

                    // Parse total calories with null check
                    Object totalCaloriesObj = dayData.get("totalCalories");
                    if (totalCaloriesObj != null) {
                        dayPlan.setTotalCalories(((Number) totalCaloriesObj).intValue());
                    }

                    // Parse macronutrients with null checks
                    Map<String, Object> macrosData = (Map<String, Object>) dayData.get("macronutrients");
                    if (macrosData != null) {
            MealPlan.Macronutrients macros = new MealPlan.Macronutrients();
                        Object proteinsObj = macrosData.get("proteins");
                        Object fatsObj = macrosData.get("fats");
                        Object carbsObj = macrosData.get("carbs");
                        
                        if (proteinsObj != null) macros.setProteins(((Number) proteinsObj).doubleValue());
                        if (fatsObj != null) macros.setFats(((Number) fatsObj).doubleValue());
                        if (carbsObj != null) macros.setCarbs(((Number) carbsObj).doubleValue());
                        
                        dayPlan.setMacronutrients(macros);
                        logger.info("Parsed macros for {}: proteins={}, fats={}, carbs={}", dayData.get("day"), macros.getProteins(), macros.getFats(), macros.getCarbs());
                    }

                    week.add(dayPlan);
                }
                mealPlan.setWeek(week);
                logger.info("Parsed week for meal plan: {}", objectMapper.writeValueAsString(week));
            }

            // Set dates
            LocalDate today = LocalDate.now();
            mealPlan.setStartDate(today);
            mealPlan.setEndDate(today.plusDays(6)); // Weekly plan

            return mealPlan;
        } catch (Exception e) {
            logger.error("Error parsing AI response: {}", e.getMessage());
            throw new RuntimeException("Error parsing AI response: " + e.getMessage(), e);
        }
    }

    private String createPrompt(User user) {
        return String.format("""
            Ты — профессиональный ИИ-диетолог. Составь подробный персонализированный план питания на 7 дней (неделю) для пользователя с такими характеристиками:
            - Возраст: %d
            - Пол: %s
            - Вес: %.1f кг
            - Рост: %.1f см
            - Уровень активности: %s
            - Цель: %s

            ВАЖНО: Верни ТОЛЬКО валидный JSON, без пояснений, markdown, комментариев и других символов. Никаких пояснений, только JSON!

            Требуемая структура:
            {
              "week": [
                    {
                  "day": "Понедельник",
                  "breakfast": { ... },
                  "lunch": { ... },
                  "dinner": { ... },
                  "snack": { ... },
                "totalCalories": number,
                "macronutrients": {
                    "proteins": number,
                    "fats": number,
                    "carbs": number
                }
                },
                ... (ещё 6 дней)
              ]
            }

            Для каждого дня недели (Понедельник, Вторник, ..., Воскресенье) укажи уникальные блюда на завтрак, обед, ужин и перекус. Не повторяй блюда в течение недели.
            Каждый прием пищи должен содержать: название, тип (breakfast/lunch/dinner/snack), калории, белки, жиры, углеводы, ингредиенты (массив строк), рецепт (строка).
            
            ВАЖНО: Для каждого ингредиента в рецепте укажи приблизительное количество в граммах или миллилитрах. Например:
            - Овсяные хлопья (50 г)
            - Молоко (200 мл)
            - Банан (120 г)
            - Мед (15 г)
            
            Все блюда, ингредиенты и рецепты — на русском языке, с учетом сезонности и традиций России.
            Калорийность и макроэлементы должны быть реалистичными и соответствовать целям пользователя.
            
            Пример одного дня:
            {
              "day": "Понедельник",
              "breakfast": {
                "name": "Овсяная каша с ягодами",
                "type": "breakfast",
                "calories": 350,
                "proteins": 12,
                "fats": 8,
                "carbs": 55,
                "ingredients": [
                  "Овсяные хлопья (50 г)",
                  "Молоко 2.5%% (200 мл)",
                  "Мед (15 г)",
                  "Смесь ягод (100 г)"
                ],
                "recipe": "Сварить овсяные хлопья (50 г) на молоке (200 мл), добавить мед (15 г) и свежие ягоды (100 г)"
              },
              "lunch": { ... },
              "dinner": { ... },
              "snack": { ... },
              "totalCalories": 2200,
              "macronutrients": { "proteins": 120, "fats": 70, "carbs": 250 }
            }
            Верни ТОЛЬКО JSON с ключом "week" (7 дней), без других данных.
            """,
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal()
        );
    }

    public MealPlan.WeeklyDayPlan generateSingleDayMealPlan(User user, String day) {
        try {
            String prompt = createSingleDayPrompt(user, day);
            logger.info("Generated prompt for single day: {}", prompt);
            
            String aiResponse = geminiClient.generateMealPlan(prompt);
            logger.info("Received response for single day: {}", aiResponse);
            
            return parseSingleDayResponse(aiResponse);
        } catch (Exception e) {
            logger.warn("Failed to generate single day meal plan using AI service, falling back to basic meal plan: {}", e.getMessage());
            return generateFallbackSingleDayPlan(user, day);
        }
    }

    private MealPlan.WeeklyDayPlan generateFallbackSingleDayPlan(User user, String day) {
        Map<String, Double> macros = calculateMacros(user);
        double totalCalories = calculateTotalCalories(user);

        MealPlan.WeeklyDayPlan dayPlan = new MealPlan.WeeklyDayPlan();
        dayPlan.setDate(day);
        
        // Generate one meal of each type
        List<Meal> breakfasts = generateBreakfast(user, macros);
        List<Meal> lunches = generateLunch(user, macros);
        List<Meal> dinners = generateDinner(user, macros);
        List<Meal> snacks = generateSnacks(user, macros);

        if (!breakfasts.isEmpty()) dayPlan.setBreakfast(breakfasts.get(0));
        if (!lunches.isEmpty()) dayPlan.setLunch(lunches.get(0));
        if (!dinners.isEmpty()) dayPlan.setDinner(dinners.get(0));
        if (!snacks.isEmpty()) dayPlan.setSnack(snacks.get(0));

        dayPlan.setTotalCalories((int) totalCalories);

        MealPlan.Macronutrients macronutrients = new MealPlan.Macronutrients();
        macronutrients.setProteins(macros.get("protein"));
        macronutrients.setFats(macros.get("fat"));
        macronutrients.setCarbs(macros.get("carbs"));
        dayPlan.setMacronutrients(macronutrients);

        return dayPlan;
    }

    private String createSingleDayPrompt(User user, String day) {
        return String.format("""
            Ты — профессиональный ИИ-диетолог. Составь подробный персонализированный план питания на один день (%s) для пользователя с такими характеристиками:
            - Возраст: %d
            - Пол: %s
            - Вес: %.1f кг
            - Рост: %.1f см
            - Уровень активности: %s
            - Цель: %s

            ВАЖНО: Верни ТОЛЬКО валидный JSON, без пояснений, markdown, комментариев и других символов. Никаких пояснений, только JSON!

            Требуемая структура:
            {
              "day": "%s",
              "breakfast": { ... },
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

            Каждый прием пищи должен содержать: название, тип (breakfast/lunch/dinner/snack), калории, белки, жиры, углеводы, ингредиенты (массив строк), рецепт (строка).
            
            ВАЖНО: Для каждого ингредиента в рецепте укажи приблизительное количество в граммах или миллилитрах. Например:
            - Овсяные хлопья (50 г)
            - Молоко (200 мл)
            - Банан (120 г)
            - Мед (15 г)
            
            Все блюда, ингредиенты и рецепты — на русском языке, с учетом сезонности и традиций России.
            Калорийность и макроэлементы должны быть реалистичными и соответствовать целям пользователя.
            
            Пример ответа:
            {
              "day": "%s",
              "breakfast": {
                "name": "Овсяная каша с ягодами",
                "type": "breakfast",
                "calories": 350,
                "proteins": 12,
                "fats": 8,
                "carbs": 55,
                "ingredients": [
                  "Овсяные хлопья (50 г)",
                  "Молоко 2.5%% (200 мл)",
                  "Мед (15 г)",
                  "Смесь ягод (100 г)"
                ],
                "recipe": "Сварить овсяные хлопья (50 г) на молоке (200 мл), добавить мед (15 г) и свежие ягоды (100 г)"
              },
              "lunch": { ... },
              "dinner": { ... },
              "snack": { ... },
              "totalCalories": 2200,
              "macronutrients": { "proteins": 120, "fats": 70, "carbs": 250 }
            }
            Верни ТОЛЬКО JSON для одного дня, без других данных.
            """,
            day,
            user.getAge(),
            user.getGender(),
            user.getWeight(),
            user.getHeight(),
            user.getActivityLevel(),
            user.getGoal(),
            day,
            day
        );
    }

    private MealPlan.WeeklyDayPlan parseSingleDayResponse(String response) {
        try {
            String content = null;
            // Try to parse the response as JSON (OpenRouter format)
            if (response.trim().startsWith("{")) {
                JsonNode root = objectMapper.readTree(response);
                content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
            } else {
                // Fallback: response is not JSON, treat as raw content
                content = response;
            }

            logger.info("Extracted content from single day response: {}", content);

            // Remove markdown code block markers if present
            content = content.trim();
            if (content.startsWith("```")) {
                int firstNewline = content.indexOf('\n');
                if (firstNewline != -1) {
                    content = content.substring(firstNewline + 1);
                } else {
                    content = content.replaceFirst("^```[a-zA-Z]*", "");
                }
            }
            if (content.endsWith("```")) {
                content = content.substring(0, content.lastIndexOf("```"));
            }
            content = content.trim();
            if (!content.startsWith("{")) {
                int startIndex = content.indexOf("{");
                if (startIndex == -1) {
                    throw new RuntimeException("No JSON object found in response");
                }
                content = content.substring(startIndex);
            }

            // Parse the content as our meal plan JSON
            Map<String, Object> data = objectMapper.readValue(content, Map.class);
            MealPlan.WeeklyDayPlan dayPlan = new MealPlan.WeeklyDayPlan();
            dayPlan.setDate((String) data.get("day"));
            
            // Parse meals with null checks
            Map<String, Object> breakfastData = (Map<String, Object>) data.get("breakfast");
            if (breakfastData != null) {
                breakfastData.remove("type");
                if (breakfastData.containsKey("carbs")) {
                    breakfastData.put("carbohydrates", breakfastData.get("carbs"));
                    breakfastData.remove("carbs");
                }
                Meal breakfast = objectMapper.convertValue(breakfastData, Meal.class);
                breakfast.setMealType(Meal.MealType.BREAKFAST);
                breakfast.setDescription((String) breakfastData.get("description"));
                breakfast.setRecipe((String) breakfastData.get("recipe"));
                breakfast.setImageUrl((String) breakfastData.get("imageUrl"));
                dayPlan.setBreakfast(breakfast);
            }
            
            Map<String, Object> lunchData = (Map<String, Object>) data.get("lunch");
            if (lunchData != null) {
                lunchData.remove("type");
                if (lunchData.containsKey("carbs")) {
                    lunchData.put("carbohydrates", lunchData.get("carbs"));
                    lunchData.remove("carbs");
                }
                Meal lunch = objectMapper.convertValue(lunchData, Meal.class);
                lunch.setMealType(Meal.MealType.LUNCH);
                lunch.setDescription((String) lunchData.get("description"));
                lunch.setRecipe((String) lunchData.get("recipe"));
                lunch.setImageUrl((String) lunchData.get("imageUrl"));
                dayPlan.setLunch(lunch);
            }
            
            Map<String, Object> dinnerData = (Map<String, Object>) data.get("dinner");
            if (dinnerData != null) {
                dinnerData.remove("type");
                if (dinnerData.containsKey("carbs")) {
                    dinnerData.put("carbohydrates", dinnerData.get("carbs"));
                    dinnerData.remove("carbs");
                }
                Meal dinner = objectMapper.convertValue(dinnerData, Meal.class);
                dinner.setMealType(Meal.MealType.DINNER);
                dinner.setDescription((String) dinnerData.get("description"));
                dinner.setRecipe((String) dinnerData.get("recipe"));
                dinner.setImageUrl((String) dinnerData.get("imageUrl"));
                dayPlan.setDinner(dinner);
            }
            
            Map<String, Object> snackData = (Map<String, Object>) data.get("snack");
            if (snackData != null) {
                snackData.remove("type");
                if (snackData.containsKey("carbs")) {
                    snackData.put("carbohydrates", snackData.get("carbs"));
                    snackData.remove("carbs");
                }
                Meal snack = objectMapper.convertValue(snackData, Meal.class);
                snack.setMealType(Meal.MealType.SNACK);
                snack.setDescription((String) snackData.get("description"));
                snack.setRecipe((String) snackData.get("recipe"));
                snack.setImageUrl((String) snackData.get("imageUrl"));
                dayPlan.setSnack(snack);
            }

            // Parse total calories with null check
            Object totalCaloriesObj = data.get("totalCalories");
            if (totalCaloriesObj != null) {
                dayPlan.setTotalCalories(((Number) totalCaloriesObj).intValue());
            }

            // Parse macronutrients with null checks
            Map<String, Object> macrosData = (Map<String, Object>) data.get("macronutrients");
            if (macrosData != null) {
                MealPlan.Macronutrients macros = new MealPlan.Macronutrients();
                Object proteinsObj = macrosData.get("proteins");
                Object fatsObj = macrosData.get("fats");
                Object carbsObj = macrosData.get("carbs");
                
                if (proteinsObj != null) macros.setProteins(((Number) proteinsObj).doubleValue());
                if (fatsObj != null) macros.setFats(((Number) fatsObj).doubleValue());
                if (carbsObj != null) macros.setCarbs(((Number) carbsObj).doubleValue());
                
                dayPlan.setMacronutrients(macros);
                logger.info("Parsed macros for {}: proteins={}, fats={}, carbs={}", data.get("day"), macros.getProteins(), macros.getFats(), macros.getCarbs());
            }

            return dayPlan;
        } catch (Exception e) {
            logger.error("Error parsing single day AI response: {}", e.getMessage());
            throw new RuntimeException("Error parsing single day AI response: " + e.getMessage(), e);
        }
    }

    public MealPlan.WeeklyDayPlan regenerateDayMealPlan(User user, String day, MealPlan currentMealPlan) {
        try {
            // Генерируем новый план на день
            MealPlan.WeeklyDayPlan newDayPlan = generateSingleDayMealPlan(user, day);
            
            // Проверяем, что новый план не содержит блюд, которые уже есть в других днях недели
            if (currentMealPlan != null && currentMealPlan.getWeek() != null) {
                List<String> existingMeals = new ArrayList<>();
                
                // Собираем все существующие блюда из недельного плана
                for (MealPlan.WeeklyDayPlan existingDay : currentMealPlan.getWeek()) {
                    if (!existingDay.getDate().equals(day)) { // Пропускаем текущий день
                        if (existingDay.getBreakfast() != null) {
                            existingMeals.add(existingDay.getBreakfast().getName());
                        }
                        if (existingDay.getLunch() != null) {
                            existingMeals.add(existingDay.getLunch().getName());
                        }
                        if (existingDay.getDinner() != null) {
                            existingMeals.add(existingDay.getDinner().getName());
                        }
                        if (existingDay.getSnack() != null) {
                            existingMeals.add(existingDay.getSnack().getName());
                        }
                    }
                }
                
                // Проверяем новые блюда на уникальность
                boolean hasDuplicates = false;
                if (newDayPlan.getBreakfast() != null && existingMeals.contains(newDayPlan.getBreakfast().getName())) {
                    hasDuplicates = true;
                }
                if (newDayPlan.getLunch() != null && existingMeals.contains(newDayPlan.getLunch().getName())) {
                    hasDuplicates = true;
                }
                if (newDayPlan.getDinner() != null && existingMeals.contains(newDayPlan.getDinner().getName())) {
                    hasDuplicates = true;
                }
                if (newDayPlan.getSnack() != null && existingMeals.contains(newDayPlan.getSnack().getName())) {
                    hasDuplicates = true;
                }
                
                // Если есть дубликаты, генерируем план заново
                if (hasDuplicates) {
                    logger.info("Found duplicate meals in regenerated plan, generating new one");
                    return regenerateDayMealPlan(user, day, currentMealPlan);
                }
            }
            
            return newDayPlan;
        } catch (Exception e) {
            logger.warn("Failed to regenerate day meal plan, falling back to basic meal plan: {}", e.getMessage());
            return generateFallbackSingleDayPlan(user, day);
        }
    }
} 