import 'dart:convert';
import 'package:http/http.dart' as http;

class APIService {
  static final APIService _instance = APIService._internal();
  factory APIService() => _instance;
  APIService._internal();

  static const String baseUrl = 'http://192.168.100.108:8080/api';
  final String? token;

  APIService({required this.token});

  Map<String, String> _getHeaders({String? token}) {
    return {
      'Authorization': 'Bearer $token',
    };
  }

  Future<MealPlan?> fetchSavedMealPlan(int userId) async {
    if (token == null) {
      throw Exception('Not authenticated');
    }

    print('Fetching saved meal plan for user $userId');
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/meal-plans/user/$userId/current'),
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
          'Authorization': 'Bearer $token',
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
} 