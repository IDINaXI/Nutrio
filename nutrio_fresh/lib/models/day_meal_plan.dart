import 'meal.dart';

class DayMealPlan {
  final int? id;
  final Meal breakfast;
  final Meal lunch;
  final Meal dinner;
  final Meal snack;
  final double totalCalories;
  final Map<String, double> macronutrients;
  final String date;

  DayMealPlan({
    this.id,
    required this.breakfast,
    required this.lunch,
    required this.dinner,
    required this.snack,
    required this.totalCalories,
    required this.macronutrients,
    required this.date,
  });

  factory DayMealPlan.fromJson(Map<String, dynamic> json) {
    return DayMealPlan(
      id: json['id'] as int?,
      breakfast: Meal.fromJson(json['breakfast'] as Map<String, dynamic>),
      lunch: Meal.fromJson(json['lunch'] as Map<String, dynamic>),
      dinner: Meal.fromJson(json['dinner'] as Map<String, dynamic>),
      snack: Meal.fromJson(json['snack'] as Map<String, dynamic>),
      totalCalories: (json['totalCalories'] as num?)?.toDouble() ?? 0.0,
      macronutrients: Map<String, double>.from(
        (json['macronutrients'] as Map?)?.map(
          (key, value) => MapEntry(key, (value as num?)?.toDouble() ?? 0.0),
        ) ?? {},
      ),
      date: json['date'] as String? ?? json['day'] as String? ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'breakfast': breakfast.toJson(),
      'lunch': lunch.toJson(),
      'dinner': dinner.toJson(),
      'snack': snack.toJson(),
      'totalCalories': totalCalories,
      'macronutrients': macronutrients,
      'date': date,
    };
  }
} 