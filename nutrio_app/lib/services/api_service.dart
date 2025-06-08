import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/auth_user.dart';
import '../models/meal_plan.dart';
import '../models/day_meal_plan.dart';
import '../models/weight_entry.dart';

class ApiService {
  static final ApiService _instance = ApiService._internal();
  factory ApiService() => _instance;
  ApiService._internal();

  static const String baseUrl = 'https://nutrio.onrender.com/api';
  String? _token;

  void setToken(String token) {
    _token = token;
  }

  Map<String, String> _getHeaders({String? token}) {
    final headers = {
      'Content-Type': 'application/json',
      'Accept': 'application/json',
    };

    if (token != null) {
      headers['Authorization'] = 'Bearer $token';
    }

    return headers;
  }

  Future<Map<String, dynamic>> get(String endpoint, {String? token}) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl$endpoint'),
        headers: _getHeaders(token: token),
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else if (response.statusCode == 401) {
        throw Exception('Unauthorized: Please login again');
      } else if (response.statusCode == 403) {
        throw Exception('Forbidden: You don\'t have permission to access this resource');
      } else if (response.statusCode == 404) {
        throw Exception('Not found: The requested resource was not found');
      } else {
        throw Exception('API Error: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      if (e is Exception) rethrow;
      throw Exception('Network error: $e');
    }
  }

  Future<Map<String, dynamic>> post(String endpoint, Map<String, dynamic> data, {String? token}) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl$endpoint'),
        headers: _getHeaders(token: token),
        body: jsonEncode(data),
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return jsonDecode(response.body);
      } else if (response.statusCode == 400) {
        throw Exception('Bad request: ${response.body}');
      } else if (response.statusCode == 401) {
        throw Exception('Unauthorized: Please login again');
      } else if (response.statusCode == 403) {
        throw Exception('Forbidden: You don\'t have permission to access this resource');
      } else if (response.statusCode == 409) {
        throw Exception('Conflict: ${response.body}');
      } else {
        throw Exception('API Error: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      if (e is Exception) rethrow;
      throw Exception('Network error: $e');
    }
  }

  Future<Map<String, dynamic>> put(String endpoint, Map<String, dynamic> data, {required String token}) async {
    try {
      final response = await http.put(
        Uri.parse('$baseUrl$endpoint'),
        headers: _getHeaders(token: token),
        body: jsonEncode(data),
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      } else if (response.statusCode == 400) {
        throw Exception('Bad request: ${response.body}');
      } else if (response.statusCode == 401) {
        throw Exception('Unauthorized: Please login again');
      } else if (response.statusCode == 403) {
        throw Exception('Forbidden: You don\'t have permission to access this resource');
      } else if (response.statusCode == 404) {
        throw Exception('Not found: The requested resource was not found');
      } else {
        throw Exception('API Error: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      if (e is Exception) rethrow;
      throw Exception('Network error: $e');
    }
  }

  Future<void> delete(String endpoint, {required String token}) async {
    try {
      final response = await http.delete(
        Uri.parse('$baseUrl$endpoint'),
        headers: _getHeaders(token: token),
      );

      if (response.statusCode != 200 && response.statusCode != 204) {
        if (response.statusCode == 401) {
          throw Exception('Unauthorized: Please login again');
        } else if (response.statusCode == 403) {
          throw Exception('Forbidden: You don\'t have permission to access this resource');
        } else if (response.statusCode == 404) {
          throw Exception('Not found: The requested resource was not found');
        } else {
          throw Exception('API Error: ${response.statusCode} - ${response.body}');
        }
      }
    } catch (e) {
      if (e is Exception) rethrow;
      throw Exception('Network error: $e');
    }
  }

  Future<MealPlan> generateMealPlan(AuthUser user) async {
    try {
      if (_token == null) {
        throw Exception('Not authenticated');
      }

      print('Generating meal plan for user: ${user.name}');
      print('User data: age=${user.age}, weight=${user.weight}, height=${user.height}, activityLevel=${user.activityLevel}, goal=${user.goal}');

      final userData = {
        'name': user.name,
        'email': user.email,
        'age': user.age,
        'height': user.height,
        'weight': user.weight,
        'gender': user.gender,
        'goal': user.goal,
        'activityLevel': user.activityLevel,
        'allergies': user.allergies,
      };

      print('Sending user data to backend: $userData');

      final response = await http.post(
        Uri.parse('$baseUrl/ai/generate'),
        headers: {
          'Authorization': 'Bearer $_token',
          'Content-Type': 'application/json',
        },
        body: jsonEncode(userData),
      );

      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return MealPlan.fromJson(data);
      } else if (response.statusCode == 400) {
        throw Exception('Invalid request: ${response.body}');
      } else if (response.statusCode == 401) {
        throw Exception('Unauthorized: Please login again');
      } else if (response.statusCode == 403) {
        throw Exception('Forbidden: You don\'t have permission to access this resource');
      } else if (response.statusCode == 503) {
        final errorData = jsonDecode(response.body);
        final errorMessage = errorData['error'] ?? 'Сервис генерации плана питания временно недоступен';
        throw Exception(errorMessage);
      } else {
        throw Exception('Не удалось сгенерировать план питания. Пожалуйста, попробуйте позже.');
      }
    } on Exception catch (e) {
      print('Generate meal plan error: $e');
      rethrow;
    } catch (e) {
      throw Exception('Произошла ошибка при генерации плана питания. Пожалуйста, попробуйте позже.');
    }
  }

  Future<DayMealPlan> generateDayMealPlan() async {
    final response = await http.post(
      Uri.parse('$baseUrl/ai/generate-day-plan'),
      headers: _getHeaders(token: _token),
    );
    if (response.statusCode == 200) {
      return DayMealPlan.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to generate day meal plan');
    }
  }

  Future<List<DayMealPlan>> getDayMealPlanHistory() async {
    final response = await http.get(
      Uri.parse('$baseUrl/ai/day-plan-history'),
      headers: _getHeaders(token: _token),
    );
    if (response.statusCode == 200) {
      final List<dynamic> data = jsonDecode(response.body);
      return data.map((e) => DayMealPlan.fromJson(e)).toList();
    } else {
      throw Exception('Failed to load day meal plan history');
    }
  }

  Future<MealPlan?> fetchSavedMealPlan(int userId) async {
    if (_token == null) {
      throw Exception('Not authenticated');
    }

    print('Fetching saved meal plan for user $userId');
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/meal-plans/user/$userId/current'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': 'Bearer $_token',
        },
      );
      print('Response status: ${response.statusCode}');
      print('Response body: ${response.body}');
      
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        print('Parsed meal plan data: $data');
        return MealPlan.fromJson(data);
      } else if (response.statusCode == 404) {
        print('No meal plan found for user');
        return null;
      } else if (response.statusCode == 401 || response.statusCode == 403) {
        throw Exception('Unauthorized: Please login again');
      } else {
        throw Exception('Failed to fetch meal plan: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      print('Error fetching meal plan: $e');
      rethrow;
    }
  }

  Future<void> saveMealPlan(int userId, MealPlan plan) async {
    await http.post(
      Uri.parse('$baseUrl/meal-plans/user/$userId/save'),
      headers: _getHeaders(token: _token),
      body: jsonEncode(plan.toJson()),
    );
  }

  Future<List<WeightEntry>> fetchWeightHistory(int userId) async {
    if (_token == null) throw Exception('Not authenticated');
    final response = await http.get(
      Uri.parse('$baseUrl/users/$userId/weights'),
      headers: _getHeaders(token: _token),
    );
    if (response.statusCode == 200) {
      final data = jsonDecode(response.body) as List;
      return data.map((e) => WeightEntry.fromJson(e)).toList();
    } else {
      throw Exception('Failed to fetch weight history: ${response.statusCode}');
    }
  }

  Future<void> addWeightEntry(int userId, double weight) async {
    if (_token == null) throw Exception('Not authenticated');
    final response = await http.post(
      Uri.parse('$baseUrl/users/$userId/weights'),
      headers: _getHeaders(token: _token),
      body: jsonEncode({'weight': weight}),
    );
    if (response.statusCode != 200 && response.statusCode != 201) {
      throw Exception('Failed to add weight entry: ${response.statusCode}');
    }
  }

  Future<DayMealPlan> regenerateDay(int userId, String day, MealPlan currentMealPlan) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/meal-plans/regenerate-day?userId=$userId&day=$day'),
        headers: _getHeaders(token: _token),
        body: jsonEncode(currentMealPlan.toJson()),
      );

      if (response.statusCode == 200) {
        final responseData = jsonDecode(response.body);
        print('Response data: $responseData'); // Для отладки
        
        if (responseData == null) {
          throw Exception('Received null response from server');
        }

        // Преобразуем данные для каждого приема пищи
        ['breakfast', 'lunch', 'dinner', 'snack'].forEach((mealType) {
          if (responseData[mealType] != null) {
            final meal = responseData[mealType] as Map<String, dynamic>;
            // Преобразуем макроэлементы в правильный формат
            final mealMacros = {
              'protein': meal['proteins']?.toDouble() ?? 0.0,
              'fat': meal['fats']?.toDouble() ?? 0.0,
              'carbs': meal['carbohydrates']?.toDouble() ?? 0.0,
            };
            meal['macros'] = mealMacros;
            meal['calories'] = meal['calories']?.toDouble() ?? 0.0;
          }
        });

        // Преобразуем общие макроэлементы
        final macros = responseData['macronutrients'] as Map<String, dynamic>;
        final formattedMacros = {
          'proteins': macros['proteins']?.toDouble() ?? 0.0,
          'fats': macros['fats']?.toDouble() ?? 0.0,
          'carbs': macros['carbs']?.toDouble() ?? 0.0,
        };
        responseData['macronutrients'] = formattedMacros;

        // Преобразуем общие калории
        responseData['totalCalories'] = responseData['totalCalories']?.toDouble() ?? 0.0;

        return DayMealPlan.fromJson(responseData);
      } else {
        throw Exception('Failed to regenerate day: ${response.body}');
      }
    } catch (e) {
      print('Error in regenerateDay: $e'); // Для отладки
      throw Exception('Failed to regenerate day: $e');
    }
  }
} 