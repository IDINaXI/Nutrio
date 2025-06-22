import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/pill_reminder.dart';
import 'auth_service.dart';

class PillReminderService {
  static const String baseUrl = 'https://nutrio-production.up.railway.app/api/pill-reminders';

  String? _getToken() => AuthService().token;

  Map<String, String> _getHeaders() {
    final token = _getToken();
    return {
      'Content-Type': 'application/json',
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<List<PillReminder>> getUserReminders(int userId) async {
    final headers = _getHeaders();
    final response = await http.get(Uri.parse('$baseUrl/user/$userId'), headers: headers);

    if (response.statusCode == 200) {
      List<dynamic> jsonList = json.decode(response.body);
      return jsonList.map((json) => PillReminder.fromJson(json)).toList();
    } else {
      throw Exception('Failed to load reminders');
    }
  }

  Future<PillReminder> addReminder(PillReminder reminder) async {
    final headers = _getHeaders();
    final response = await http.post(
      Uri.parse(baseUrl),
      headers: headers,
      body: json.encode(reminder.toJson()..remove('id')),
    );

    if (response.statusCode == 200) {
      return PillReminder.fromJson(json.decode(response.body));
    } else {
      throw Exception('Failed to add reminder');
    }
  }

  Future<void> deleteReminder(int id) async {
    final headers = _getHeaders();
    final response = await http.delete(Uri.parse('$baseUrl/$id'), headers: headers);

    if (response.statusCode != 200) {
      throw Exception('Failed to delete reminder');
    }
  }
} 