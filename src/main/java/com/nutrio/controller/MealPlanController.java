package com.nutrio.controller;

import com.nutrio.model.DayMealPlan;
import com.nutrio.model.MealPlan;
import com.nutrio.model.User;
import com.nutrio.service.AuthService;
import com.nutrio.service.MealPlanService;
import com.nutrio.service.UserService;
import com.nutrio.repository.MealPlanRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Slf4j
@RestController
@RequestMapping("/api/meal-plans")
@Tag(name = "Meal Plans", description = "API for managing meal plans")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {
    "http://192.168.100.112:3000",
    "http://127.0.0.1:3000"
})
public class MealPlanController {
    private static final Logger logger = LoggerFactory.getLogger(MealPlanController.class);

    private final MealPlanService mealPlanService;
    private final UserService userService;
    private final MealPlanRepository mealPlanRepository;

    @Autowired
    public MealPlanController(MealPlanService mealPlanService, UserService userService, MealPlanRepository mealPlanRepository) {
        this.mealPlanService = mealPlanService;
        this.userService = userService;
        this.mealPlanRepository = mealPlanRepository;
    }

    @PostMapping("/generate")
    @Operation(
        summary = "Generate a new meal plan",
        description = "Generates a personalized meal plan based on user's profile"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meal plan generated successfully",
            content = @Content(schema = @Schema(implementation = MealPlan.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<MealPlan> generateMealPlan(@RequestParam Long userId) {
        logger.info("Received request to generate meal plan for user: {}", userId);
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            MealPlan mealPlan = mealPlanService.generateMealPlan(user);
            logger.info("Successfully generated meal plan for user: {}", userId);
            return ResponseEntity.ok(mealPlan);
        } catch (Exception e) {
            logger.error("Error generating meal plan: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate/custom")
    @Operation(
        summary = "Generate a custom meal plan",
        description = "Generates a personalized meal plan based on provided parameters"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meal plan generated successfully",
            content = @Content(schema = @Schema(implementation = MealPlan.class))),
        @ApiResponse(responseCode = "400", description = "Invalid parameters provided"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public ResponseEntity<MealPlan> generateCustomMealPlan(
        @Parameter(hidden = true) Authentication authentication,
        @RequestBody Map<String, Object> parameters
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        boolean userUpdated = false;

        // Update user parameters from request
        if (parameters.containsKey("gender")) {
            user.setGender(User.Gender.valueOf(((String) parameters.get("gender")).toUpperCase()));
            userUpdated = true;
        }
        if (parameters.containsKey("age")) {
            user.setAge(((Number) parameters.get("age")).intValue());
            userUpdated = true;
        }
        if (parameters.containsKey("weight")) {
            user.setWeight(((Number) parameters.get("weight")).doubleValue());
            userUpdated = true;
        }
        if (parameters.containsKey("height")) {
            user.setHeight(((Number) parameters.get("height")).doubleValue());
            userUpdated = true;
        }
        if (parameters.containsKey("activityLevel")) {
            user.setActivityLevel(User.ActivityLevel.valueOf(((String) parameters.get("activityLevel")).toUpperCase()));
            userUpdated = true;
        }
        if (parameters.containsKey("goal")) {
            user.setGoal(User.Goal.valueOf(((String) parameters.get("goal")).toUpperCase()));
            userUpdated = true;
        }
        if (parameters.containsKey("allergies")) {
            user.setAllergies((List<String>) parameters.get("allergies"));
            userUpdated = true;
        }

        // Save updated user data
        if (userUpdated) {
            user = userService.save(user);
        }

        MealPlan mealPlan = mealPlanService.generateMealPlan(user);
        return ResponseEntity.ok(mealPlan);
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user's meal plans",
        description = "Retrieves all meal plans for the authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meal plans retrieved successfully",
            content = @Content(schema = @Schema(implementation = MealPlan.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<MealPlan>> getUserMealPlans(@PathVariable Long userId) {
        logger.info("Received request to get meal plans for user: {}", userId);
        try {
            List<MealPlan> mealPlans = mealPlanService.getUserMealPlans(userId);
            return ResponseEntity.ok(mealPlans);
        } catch (Exception e) {
            logger.error("Error getting meal plans: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/day/generate")
    @Operation(
        summary = "Generate a day meal plan",
        description = "Generates a meal plan for a specific day"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Day meal plan generated successfully",
            content = @Content(schema = @Schema(implementation = DayMealPlan.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<DayMealPlan> generateDayMealPlan(@RequestParam Long userId) {
        logger.info("Received request to generate day meal plan for user: {}", userId);
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            DayMealPlan dayMealPlan = mealPlanService.generateDayMealPlan(user);
            logger.info("Successfully generated day meal plan for user: {}", userId);
            return ResponseEntity.ok(dayMealPlan);
        } catch (Exception e) {
            logger.error("Error generating day meal plan: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/day-history")
    public ResponseEntity<List<DayMealPlan>> getUserDayMealPlans(
        @Parameter(hidden = true) Authentication authentication
    ) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        List<DayMealPlan> plans = mealPlanService.getUserDayMealPlans(user.getId());
        return ResponseEntity.ok(plans);
    }

    @GetMapping("/user/{userId}/current")
    public ResponseEntity<MealPlan> getCurrentMealPlan(@PathVariable Long userId) {
        return mealPlanRepository.findByUserId(userId).stream()
            .sorted(Comparator.comparing(MealPlan::getCreatedAt).reversed())
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/user/{userId}/save")
    public ResponseEntity<MealPlan> saveMealPlan(@PathVariable Long userId, @RequestBody MealPlan plan) {
        User user = userService.getUserById(userId);
        plan.setUser(user);
        plan.setCreatedAt(java.time.LocalDateTime.now());
        if (plan.getStartDate() == null) plan.setStartDate(java.time.LocalDate.now());
        if (plan.getEndDate() == null) plan.setEndDate(plan.getStartDate().plusDays(6));
        mealPlanRepository.save(plan);
        return ResponseEntity.ok(plan);
    }

    @PostMapping("/regenerate-day")
    @Operation(
        summary = "Regenerate a specific day in meal plan",
        description = "Regenerates meal plan for a specific day while maintaining uniqueness across the week"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Day meal plan regenerated successfully",
            content = @Content(schema = @Schema(implementation = MealPlan.WeeklyDayPlan.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "404", description = "User or meal plan not found")
    })
    public ResponseEntity<MealPlan.WeeklyDayPlan> regenerateDay(
        @RequestParam Long userId,
        @RequestParam String day,
        @RequestBody MealPlan currentMealPlan
    ) {
        logger.info("Received request to regenerate meal plan for day {} for user: {}", day, userId);
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            MealPlan.WeeklyDayPlan regeneratedDay = mealPlanService.regenerateDayMealPlan(user, day, currentMealPlan);
            logger.info("Successfully regenerated meal plan for day {} for user: {}", day, userId);
            return ResponseEntity.ok(regeneratedDay);
        } catch (Exception e) {
            logger.error("Error regenerating day meal plan: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
