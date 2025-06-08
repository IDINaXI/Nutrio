class Meal {
  final String name;
  final String description;
  final List<String> ingredients;
  final String recipe;
  final double calories;
  final Map<String, double> macros;
  final String? imageUrl;

  const Meal({
    required this.name,
    required this.description,
    required this.ingredients,
    required this.recipe,
    required this.calories,
    required this.macros,
    this.imageUrl,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'description': description,
      'ingredients': ingredients,
      'recipe': recipe,
      'calories': calories,
      'macros': macros,
      'imageUrl': imageUrl,
    };
  }

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
} 