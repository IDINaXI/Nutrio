import 'meal.dart';

class WeekDayPlan {
  final String day;
  final Meal breakfast;
  final Meal lunch;
  final Meal dinner;
  final Meal snack;
  final double totalCalories;
  final Map<String, double> macronutrients;

  WeekDayPlan({
    required this.day,
    required this.breakfast,
    required this.lunch,
    required this.dinner,
    required this.snack,
    required this.totalCalories,
    required this.macronutrients,
  });

  factory WeekDayPlan.fromJson(Map<String, dynamic> json) {
    return WeekDayPlan(
      day: json['day'] as String? ?? json['date'] as String? ?? '',
      breakfast: Meal.fromJson(json['breakfast'] as Map<String, dynamic>),
      lunch: Meal.fromJson(json['lunch'] as Map<String, dynamic>),
      dinner: Meal.fromJson(json['dinner'] as Map<String, dynamic>),
      snack: Meal.fromJson(json['snack'] as Map<String, dynamic>),
      totalCalories: (json['totalCalories'] as num?)?.toDouble() ?? 0.0,
      macronutrients: json['macronutrients'] != null && json['macronutrients'] is Map
        ? {
            'proteins': (json['macronutrients']['proteins'] as num?)?.toDouble() ?? 0.0,
            'fats': (json['macronutrients']['fats'] as num?)?.toDouble() ?? 0.0,
            'carbs': (json['macronutrients']['carbs'] as num?)?.toDouble() ?? 0.0,
          }
        : <String, double>{},
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'day': day,
      'breakfast': breakfast.toJson(),
      'lunch': lunch.toJson(),
      'dinner': dinner.toJson(),
      'snack': snack.toJson(),
      'totalCalories': totalCalories,
      'macronutrients': macronutrients,
    };
  }
}

class MealPlan {
  final List<Meal> breakfast;
  final List<Meal> lunch;
  final List<Meal> dinner;
  final List<Meal> snacks;
  final double totalCalories;
  final Map<String, double> totalMacros;
  final List<WeekDayPlan> week;

  MealPlan({
    required this.breakfast,
    required this.lunch,
    required this.dinner,
    required this.snacks,
    required this.totalCalories,
    required this.totalMacros,
    this.week = const [],
  });

  factory MealPlan.fromJson(Map<String, dynamic> json) {
    return MealPlan(
      breakfast: (json['breakfast'] as List?)?.map((e) => Meal.fromJson(e)).toList() ?? [],
      lunch: (json['lunch'] as List?)?.map((e) => Meal.fromJson(e)).toList() ?? [],
      dinner: (json['dinner'] as List?)?.map((e) => Meal.fromJson(e)).toList() ?? [],
      snacks: (json['snacks'] as List?)?.map((e) => Meal.fromJson(e)).toList() ?? [],
      totalCalories: (json['totalCalories'] as num?)?.toDouble() ?? 0.0,
      totalMacros: json['macronutrients'] != null && json['macronutrients'] is Map
        ? {
            'proteins': (json['macronutrients']['proteins'] as num?)?.toDouble() ?? 0.0,
            'fats': (json['macronutrients']['fats'] as num?)?.toDouble() ?? 0.0,
            'carbs': (json['macronutrients']['carbs'] as num?)?.toDouble() ?? 0.0,
          }
        : <String, double>{},
      week: (json['week'] as List?)?.map((e) => WeekDayPlan.fromJson(e)).toList() ?? [],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'breakfast': breakfast.map((e) => e.toJson()).toList(),
      'lunch': lunch.map((e) => e.toJson()).toList(),
      'dinner': dinner.map((e) => e.toJson()).toList(),
      'snacks': snacks.map((e) => e.toJson()).toList(),
      'totalCalories': totalCalories,
      'macronutrients': totalMacros,
      'week': week.map((e) => e.toJson()).toList(),
    };
  }
}

class Meal {
  final String name;
  final String description;
  final List<String> ingredients;
  final String recipe;
  final double calories;
  final Map<String, double> macros;
  final String? imageUrl;

  Meal({
    required this.name,
    required this.description,
    required this.ingredients,
    required this.recipe,
    required this.calories,
    required this.macros,
    this.imageUrl,
  });

  factory Meal.fromJson(Map<String, dynamic> json) {
    final macros = <String, double>{};
    if (json['proteins'] != null) {
      macros['protein'] = (json['proteins'] as num?)?.toDouble() ?? 0.0;
    }
    if (json['fats'] != null) {
      macros['fat'] = (json['fats'] as num?)?.toDouble() ?? 0.0;
    }
    if (json['carbohydrates'] != null) {
      macros['carbs'] = (json['carbohydrates'] as num?)?.toDouble() ?? 0.0;
    }
    if (json['macros'] != null && json['macros'] is Map) {
      macros['protein'] = (json['macros']['protein'] as num?)?.toDouble() ?? macros['protein'] ?? 0.0;
      macros['fat'] = (json['macros']['fat'] as num?)?.toDouble() ?? macros['fat'] ?? 0.0;
      macros['carbs'] = (json['macros']['carbs'] as num?)?.toDouble() ?? macros['carbs'] ?? 0.0;
    }
    return Meal(
      name: json['name'] as String? ?? '',
      description: json['description'] as String? ?? '',
      ingredients: (json['ingredients'] as List?)?.map((e) => e as String).toList() ?? [],
      recipe: json['recipe'] as String? ?? '',
      calories: (json['calories'] as num?)?.toDouble() ?? 0.0,
      macros: macros,
      imageUrl: json['imageUrl'] as String?,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'ingredients': ingredients,
      'recipe': recipe,
      'calories': calories,
      'macros': macros,
      'proteins': macros['protein'] ?? 0.0,
      'fats': macros['fat'] ?? 0.0,
      'carbohydrates': macros['carbs'] ?? 0.0,
      'imageUrl': imageUrl,
    };
  }
} 