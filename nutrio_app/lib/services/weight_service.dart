import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/weight_entry.dart';
import 'auth_service.dart';

class WeightService {
  static const String baseUrl = 'http://192.168.100.108:8080/api/weight';

  String? _getToken() {
    return AuthService().token;
  }

  Map<String, String> _getHeaders() {
    final token = _getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<List<WeightEntry>> getUserWeightHistory(int userId) async {
    final headers = _getHeaders();
    final response = await http.get(Uri.parse('$baseUrl/user/$userId'), headers: headers);
    
    if (response.statusCode == 200) {
      List<dynamic> jsonList = json.decode(response.body);
      return jsonList.map((json) => WeightEntry.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load weight history');
    }
  }

  Future<WeightEntry> addWeightEntry(int userId, double weight, DateTime date) async {
    final headers = _getHeaders();
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: headers,
      body: json.encode({
        'userId': userId,
        'weight': weight,
        'date': date.toIso8601String(),
      }),
    );

    if (response.statusCode == 200) {
      return WeightEntry.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to add weight entry');
    }
  }

  Future<void> deleteWeightEntry(int id) async {
    final headers = _getHeaders();
    final response = await http.delete(Uri.parse('$baseUrl/$id'), headers: headers);

    if (response.statusCode != 200) {
      throw Exception('Failed to delete weight entry');
    }
  }
} 