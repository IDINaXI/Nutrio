import 'dart:convert';
import 'dart:html' as html;
import 'package:http/http.dart' as http;
import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../models/auth_user.dart';
import 'api_service.dart';

class AuthService {
  static final AuthService _instance = AuthService._internal();
  factory AuthService() => _instance;
  AuthService._internal();

  final _apiService = ApiService();
  String? _token;
  AuthUser? _currentUser;

  String? get token => _token;
  AuthUser? getCurrentUser() => _currentUser;

  String get _currentOrigin {
    return html.window.location.origin;
  }

  Map<String, String> get _headers {
    return {
      'Content-Type': 'application/json; charset=UTF-8',
      'Origin': _currentOrigin,
      if (token != null) 'Authorization': 'Bearer $token',
    };
  }

  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString('token');
    if (_token != null) {
      try {
        _currentUser = await getUserData(_token!);
        _apiService.setToken(_token!);
      } catch (e) {
        _token = null;
        _currentUser = null;
        await prefs.remove('token');
      }
    }
  }

  Future<void> login(String email, String password) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/auth/login'),
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'Accept': 'application/json; charset=UTF-8',
        },
        body: jsonEncode({
          'email': email,
          'password': password,
        }),
      );

      if (response.statusCode != 200) {
        if (response.statusCode == 401) {
          throw Exception('Invalid email or password');
        }
        throw Exception('Login failed: ${response.statusCode} - ${response.body}');
      }

      final data = jsonDecode(response.body);
      if (data['token'] == null) {
        throw Exception('Invalid response from server: missing token');
      }

      _token = data['token'];
      _apiService.setToken(_token!);

      // Get user data after successful login
      _currentUser = await getUserData(_token!);

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', _token!);
    } catch (e) {
      _token = null;
      _currentUser = null;
      rethrow;
    }
  }

  Future<void> register(
    String name,
    String email,
    String password,
    int age,
    int height,
    int weight,
    String gender,
    String goal,
    String activityLevel,
    List<String> allergies,
  ) async {
    try {
      final response = await http.post(
        Uri.parse('${ApiService.baseUrl}/auth/register'),
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'Accept': 'application/json; charset=UTF-8',
        },
        body: jsonEncode({
          'name': name,
          'email': email,
          'password': password,
          'age': age,
          'height': height,
          'weight': weight,
          'gender': gender,
          'goal': goal,
          'activityLevel': activityLevel,
          'allergies': allergies,
        }),
      );

      if (response.statusCode != 200) {
        if (response.statusCode == 409) {
          throw Exception('User with this email already exists');
        }
        throw Exception('Registration failed: ${response.statusCode} - ${response.body}');
      }

      final data = jsonDecode(response.body);
      if (data['token'] == null) {
        throw Exception('Invalid response from server: missing token');
      }

      _token = data['token'];
      _apiService.setToken(_token!);

      // Get user data after successful registration
      _currentUser = await getUserData(_token!);

      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('token', _token!);
    } catch (e) {
      _token = null;
      _currentUser = null;
      rethrow;
    }
  }

  Future<void> logout() async {
    _token = null;
    _currentUser = null;
    _apiService.setToken('');
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove('token');
  }

  Future<AuthUser> getUserData(String token) async {
    try {
      print('Getting user data with token: $token');
      
      final response = await http.get(
        Uri.parse('${ApiService.baseUrl}/auth/user'),
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'Accept': 'application/json; charset=UTF-8',
          'Authorization': 'Bearer $token',
        },
      );

      print('User data response status: ${response.statusCode}');
      print('User data response body: ${response.body}');

      if (response.statusCode != 200) {
        throw Exception('Failed to get user data: ${response.statusCode} - ${response.body}');
      }

      final data = jsonDecode(response.body);
      final user = AuthUser.fromJson(data);
      
      print('Parsed user data: ${user.name}, age=${user.age}, weight=${user.weight}, height=${user.height}, activityLevel=${user.activityLevel}, goal=${user.goal}');
      
      return user;
    } catch (e) {
      print('Error getting user data: $e');
      throw Exception('Failed to get user data: $e');
    }
  }

  Future<void> updateProfile({
    required String name,
    required int age,
    required double height,
    required double weight,
    required String gender,
    required String goal,
    required String activityLevel,
    required List<String> allergies,
    required String email,
  }) async {
    if (_token == null) throw Exception('Not authenticated');

    final response = await http.put(
      Uri.parse('${ApiService.baseUrl}/auth/profile'),
      headers: {
        'Content-Type': 'application/json; charset=UTF-8',
        'Accept': 'application/json; charset=UTF-8',
        'Authorization': 'Bearer $_token',
      },
      body: jsonEncode({
        'name': name,
        'age': age,
        'height': height,
        'weight': weight,
        'gender': gender,
        'goal': goal,
        'activityLevel': activityLevel,
        'allergies': allergies,
        'email': email,
      }),
    );

    if (response.statusCode != 200) {
      throw Exception('Failed to update profile: \\${response.statusCode} - \\${response.body}');
    }

    final data = jsonDecode(response.body);
    _currentUser = AuthUser.fromJson(data['user']);
  }
} 