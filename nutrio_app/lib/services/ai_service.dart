import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/auth_user.dart';
import '../models/meal_plan.dart';
import 'auth_service.dart';

class AIService {
  static final AIService _instance = AIService._internal();
  factory AIService() => _instance;
  AIService._internal();

  static const String baseUrl = 'http://172.20.10.2:8080/api';
  String? token;

  Map<String, String> get _headers {
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  void setToken(String newToken) {
    token = newToken;
  }

  Future<MealPlan> generateMealPlan(AuthUser user) async {
    final token = AuthService().token;
    if (token == null) throw Exception('Not authenticated');

    final response = await http.post(
      Uri.parse('$baseUrl/ai/generate-meal-plan'),
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode(user.toJson()),
    );

    if (response.statusCode == 200) {
      final data = jsonDecode(response.body);
      return MealPlan.fromJson(data);
    } else {
      throw Exception('Failed to generate meal plan: ${response.statusCode} - ${response.body}');
    }
  }

  Future<String> getMealImage(String mealName) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/ai/generate-image'),
        headers: _headers,
        body: jsonEncode({'prompt': 'Healthy food: $mealName'}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['imageUrl'] as String;
      } else {
        throw Exception('Ошибка генерации изображения: ${response.statusCode} - ${response.body}');
      }
    } catch (e) {
      print('Get meal image error: $e');
      return '';
    }
  }
} 