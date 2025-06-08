import 'meal.dart';

class DayPlan {
  final String day;
  final List<Meal> meals;
  final double calories;
  final double protein;
  final double fat;
  final double carbs;

  const DayPlan({
    required this.day,
    required this.meals,
    required this.calories,
    required this.protein,
    required this.fat,
    required this.carbs,
  });

  Map<String, dynamic> toJson() {
    return {
      'day': day,
      'meals': meals.map((meal) => meal.toJson()).toList(),
      'calories': calories,
      'protein': protein,
      'fat': fat,
      'carbs': carbs,
    };
  }

  factory DayPlan.fromJson(Map<String, dynamic> json) {
    return DayPlan(
      day: json['day'],
      meals: (json['meals'] as List)
          .map((meal) => Meal.fromJson(meal))
          .toList(),
      calories: json['calories'].toDouble(),
      protein: json['protein'].toDouble(),
      fat: json['fat'].toDouble(),
      carbs: json['carbs'].toDouble(),
    );
  }
} 