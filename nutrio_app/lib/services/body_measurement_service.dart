import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/body_measurement.dart';
import 'auth_service.dart';

class BodyMeasurementService {
  static const String baseUrl = 'https://nutrio-production.up.railway.app/api/body-measurements';

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

  Future<List<BodyMeasurement>> getUserMeasurements(int userId) async {
    final headers = _getHeaders();
    final response = await http.get(
      Uri.parse('$baseUrl/user/$userId'),
      headers: headers,
    );

    if (response.statusCode == 200) {
      List<dynamic> jsonList = json.decode(response.body);
      return jsonList.map((json) => BodyMeasurement.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load body measurements');
    }
  }

  Future<BodyMeasurement> addMeasurement(BodyMeasurement measurement) async {
    final headers = _getHeaders();
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: headers,
      body: json.encode(measurement.toJson()),
    );

    if (response.statusCode == 200) {
      return BodyMeasurement.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to add body measurement');
    }
  }

  Future<void> deleteMeasurement(int id) async {
    final headers = _getHeaders();
    final response = await http.delete(
      Uri.parse('$baseUrl/$id'),
      headers: headers,
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to delete body measurement');
    }
  }
} 