import 'package:flutter/material.dart';
import '../models/auth_user.dart';
import '../services/auth_service.dart';
import 'login_screen.dart';
import 'home_screen.dart';

class RegisterScreen extends StatefulWidget {
  const RegisterScreen({super.key});

  @override
  State<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends State<RegisterScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();
  final _confirmPasswordController = TextEditingController();
  final _ageController = TextEditingController();
  final _heightController = TextEditingController();
  final _weightController = TextEditingController();
  final _allergiesController = TextEditingController();
  final _authService = AuthService();
  bool _isLoading = false;
  bool _obscurePassword = true;
  bool _obscureConfirmPassword = true;
  String _selectedGender = 'MALE';
  String _selectedGoal = 'MAINTAIN_WEIGHT';
  String _selectedActivityLevel = 'MODERATELY_ACTIVE';

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    _ageController.dispose();
    _heightController.dispose();
    _weightController.dispose();
    _allergiesController.dispose();
    super.dispose();
  }

  Future<void> _register() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        final allergies = _allergiesController.text
            .split(',')
            .map((item) => item.trim())
            .where((item) => item.isNotEmpty)
            .toList();

        await _authService.register(
          _nameController.text,
          _emailController.text,
          _passwordController.text,
          int.parse(_ageController.text),
          int.parse(_heightController.text),
          int.parse(_weightController.text),
          _selectedGender,
          _selectedGoal,
          _selectedActivityLevel,
          allergies,
        );

        if (mounted) {
          Navigator.pushReplacement(
            context,
            MaterialPageRoute(builder: (context) => const HomeScreen()),
          );
        }
      } catch (e) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(e.toString())),
          );
        }
      } finally {
        if (mounted) {
          setState(() => _isLoading = false);
        }
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final screenWidth = MediaQuery.of(context).size.width;
    final isSmallScreen = screenWidth < 600;

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFFF8FBF5), Color(0xFFE8F5E9)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              child: Padding(
                padding: EdgeInsets.all(isSmallScreen ? 16.0 : 24.0),
                child: Card(
                  elevation: 8,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                  child: Padding(
                    padding: EdgeInsets.symmetric(
                      horizontal: isSmallScreen ? 16.0 : 28.0,
                      vertical: isSmallScreen ? 24.0 : 36.0,
                    ),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          // Логотип и приветствие
                          Center(
                            child: Column(
                              children: [
                                Container(
                                  padding: EdgeInsets.all(isSmallScreen ? 12.0 : 18.0),
                                  decoration: BoxDecoration(
                                    color: Theme.of(context).colorScheme.primary.withOpacity(0.12),
                                    shape: BoxShape.circle,
                                    boxShadow: [
                                      BoxShadow(
                                        color: Theme.of(context).colorScheme.primary.withOpacity(0.10),
                                        blurRadius: 18,
                                        offset: const Offset(0, 6),
                                      ),
                                    ],
                                  ),
                                  child: Icon(
                                    Icons.restaurant_menu,
                                    size: isSmallScreen ? 40.0 : 54.0,
                                    color: Theme.of(context).colorScheme.primary,
                                  ),
                                ),
                                SizedBox(height: isSmallScreen ? 20.0 : 28.0),
                                Text(
                                  'Создайте аккаунт',
                                  style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                                    fontWeight: FontWeight.bold,
                                    color: Theme.of(context).colorScheme.primary,
                                    fontSize: isSmallScreen ? 24.0 : null,
                                  ),
                                ),
                                const SizedBox(height: 8),
                                Text(
                                  'Зарегистрируйтесь, чтобы начать',
                                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                    color: Colors.grey[600],
                                    fontSize: isSmallScreen ? 14.0 : null,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          SizedBox(height: isSmallScreen ? 24.0 : 40.0),
                          
                          // Основные поля
                          TextFormField(
                            controller: _nameController,
                            decoration: const InputDecoration(
                              labelText: 'Имя',
                              prefixIcon: Icon(Icons.person_outline),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Пожалуйста, введите имя';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _emailController,
                            keyboardType: TextInputType.emailAddress,
                            decoration: const InputDecoration(
                              labelText: 'Электронная почта',
                              prefixIcon: Icon(Icons.email_outlined),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Пожалуйста, введите электронную почту';
                              }
                              if (!value.contains('@')) {
                                return 'Пожалуйста, введите корректный email';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _passwordController,
                            obscureText: _obscurePassword,
                            decoration: InputDecoration(
                              labelText: 'Пароль',
                              prefixIcon: const Icon(Icons.lock_outline),
                              suffixIcon: IconButton(
                                icon: Icon(
                                  _obscurePassword ? Icons.visibility_off : Icons.visibility,
                                ),
                                onPressed: () {
                                  setState(() => _obscurePassword = !_obscurePassword);
                                },
                              ),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Пожалуйста, введите пароль';
                              }
                              if (value.length < 6) {
                                return 'Пароль должен содержать не менее 6 символов';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _confirmPasswordController,
                            obscureText: _obscureConfirmPassword,
                            decoration: InputDecoration(
                              labelText: 'Подтвердите пароль',
                              prefixIcon: const Icon(Icons.lock_outline),
                              suffixIcon: IconButton(
                                icon: Icon(
                                  _obscureConfirmPassword ? Icons.visibility_off : Icons.visibility,
                                ),
                                onPressed: () {
                                  setState(() => _obscureConfirmPassword = !_obscureConfirmPassword);
                                },
                              ),
                            ),
                            validator: (value) {
                              if (value == null || value.isEmpty) {
                                return 'Пожалуйста, подтвердите пароль';
                              }
                              if (value != _passwordController.text) {
                                return 'Пароли не совпадают';
                              }
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),

                          // Возраст, рост, вес
                          if (isSmallScreen) ...[
                            TextFormField(
                              controller: _ageController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: 'Возраст',
                                prefixIcon: Icon(Icons.cake_outlined),
                              ),
                              validator: (value) {
                                if (value == null || value.isEmpty) {
                                  return 'Возраст';
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 16),
                            TextFormField(
                              controller: _heightController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: 'Рост (см)',
                                prefixIcon: Icon(Icons.height),
                              ),
                              validator: (value) {
                                if (value == null || value.isEmpty) {
                                  return 'Рост';
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 16),
                            TextFormField(
                              controller: _weightController,
                              keyboardType: TextInputType.number,
                              decoration: const InputDecoration(
                                labelText: 'Вес (кг)',
                                prefixIcon: Icon(Icons.monitor_weight_outlined),
                              ),
                              validator: (value) {
                                if (value == null || value.isEmpty) {
                                  return 'Вес';
                                }
                                return null;
                              },
                            ),
                          ] else ...[
                            Row(
                              children: [
                                Expanded(
                                  child: TextFormField(
                                    controller: _ageController,
                                    keyboardType: TextInputType.number,
                                    decoration: const InputDecoration(
                                      labelText: 'Возраст',
                                      prefixIcon: Icon(Icons.cake_outlined),
                                    ),
                                    validator: (value) {
                                      if (value == null || value.isEmpty) {
                                        return 'Возраст';
                                      }
                                      return null;
                                    },
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: TextFormField(
                                    controller: _heightController,
                                    keyboardType: TextInputType.number,
                                    decoration: const InputDecoration(
                                      labelText: 'Рост (см)',
                                      prefixIcon: Icon(Icons.height),
                                    ),
                                    validator: (value) {
                                      if (value == null || value.isEmpty) {
                                        return 'Рост';
                                      }
                                      return null;
                                    },
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: TextFormField(
                                    controller: _weightController,
                                    keyboardType: TextInputType.number,
                                    decoration: const InputDecoration(
                                      labelText: 'Вес (кг)',
                                      prefixIcon: Icon(Icons.monitor_weight_outlined),
                                    ),
                                    validator: (value) {
                                      if (value == null || value.isEmpty) {
                                        return 'Вес';
                                      }
                                      return null;
                                    },
                                  ),
                                ),
                              ],
                            ),
                          ],
                          const SizedBox(height: 16),

                          // Пол и цель
                          if (isSmallScreen) ...[
                            DropdownButtonFormField<String>(
                              value: _selectedGender,
                              decoration: const InputDecoration(
                                labelText: 'Пол',
                                prefixIcon: Icon(Icons.wc),
                              ),
                              items: const [
                                DropdownMenuItem(value: 'MALE', child: Text('Мужской')),
                                DropdownMenuItem(value: 'FEMALE', child: Text('Женский')),
                              ],
                              onChanged: (value) {
                                setState(() => _selectedGender = value!);
                              },
                            ),
                            const SizedBox(height: 16),
                            DropdownButtonFormField<String>(
                              value: _selectedGoal,
                              decoration: const InputDecoration(
                                labelText: 'Цель',
                                prefixIcon: Icon(Icons.flag_outlined),
                              ),
                              items: const [
                                DropdownMenuItem(value: 'LOSE_WEIGHT', child: Text('Похудеть')),
                                DropdownMenuItem(value: 'MAINTAIN_WEIGHT', child: Text('Поддерживать')),
                                DropdownMenuItem(value: 'GAIN_WEIGHT', child: Text('Набрать')),
                              ],
                              onChanged: (value) {
                                setState(() => _selectedGoal = value!);
                              },
                            ),
                          ] else ...[
                            Row(
                              children: [
                                Expanded(
                                  child: DropdownButtonFormField<String>(
                                    value: _selectedGender,
                                    decoration: const InputDecoration(
                                      labelText: 'Пол',
                                      prefixIcon: Icon(Icons.wc),
                                    ),
                                    items: const [
                                      DropdownMenuItem(value: 'MALE', child: Text('Мужской')),
                                      DropdownMenuItem(value: 'FEMALE', child: Text('Женский')),
                                    ],
                                    onChanged: (value) {
                                      setState(() => _selectedGender = value!);
                                    },
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: DropdownButtonFormField<String>(
                                    value: _selectedGoal,
                                    decoration: const InputDecoration(
                                      labelText: 'Цель',
                                      prefixIcon: Icon(Icons.flag_outlined),
                                    ),
                                    items: const [
                                      DropdownMenuItem(value: 'LOSE_WEIGHT', child: Text('Похудеть')),
                                      DropdownMenuItem(value: 'MAINTAIN_WEIGHT', child: Text('Поддерживать')),
                                      DropdownMenuItem(value: 'GAIN_WEIGHT', child: Text('Набрать')),
                                    ],
                                    onChanged: (value) {
                                      setState(() => _selectedGoal = value!);
                                    },
                                  ),
                                ),
                              ],
                            ),
                          ],
                          const SizedBox(height: 16),

                          // Активность
                          DropdownButtonFormField<String>(
                            value: _selectedActivityLevel,
                            decoration: const InputDecoration(
                              labelText: 'Активность',
                              prefixIcon: Icon(Icons.directions_run),
                            ),
                            items: const [
                              DropdownMenuItem(value: 'SEDENTARY', child: Text('Минимальная')),
                              DropdownMenuItem(value: 'LIGHTLY_ACTIVE', child: Text('Легкая')),
                              DropdownMenuItem(value: 'MODERATELY_ACTIVE', child: Text('Средняя')),
                              DropdownMenuItem(value: 'VERY_ACTIVE', child: Text('Высокая')),
                            ],
                            onChanged: (value) {
                              setState(() => _selectedActivityLevel = value!);
                            },
                          ),
                          const SizedBox(height: 16),

                          // Аллергии
                          TextFormField(
                            controller: _allergiesController,
                            decoration: const InputDecoration(
                              labelText: 'Аллергии и продукты, которые не нравятся',
                              hintText: 'Например: орехи, молоко, яйца, рыба',
                              prefixIcon: Icon(Icons.warning_amber_outlined),
                            ),
                          ),
                          SizedBox(height: isSmallScreen ? 24.0 : 28.0),

                          // Кнопка регистрации
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: Theme.of(context).colorScheme.primary,
                                foregroundColor: Colors.white,
                                padding: EdgeInsets.symmetric(
                                  vertical: isSmallScreen ? 16.0 : 18.0,
                                ),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(16),
                                ),
                                elevation: 2,
                              ),
                              onPressed: _isLoading ? null : _register,
                              child: _isLoading
                                  ? const SizedBox(
                                      height: 20,
                                      width: 20,
                                      child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                                    )
                                  : Text(
                                      'Зарегистрироваться',
                                      style: TextStyle(
                                        fontSize: isSmallScreen ? 16.0 : 18.0,
                                        fontWeight: FontWeight.w600,
                                      ),
                                    ),
                            ),
                          ),
                          SizedBox(height: isSmallScreen ? 16.0 : 20.0),

                          // Ссылка на вход
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              Text(
                                'Уже есть аккаунт?',
                                style: TextStyle(
                                  color: Colors.grey[600],
                                  fontSize: isSmallScreen ? 14.0 : null,
                                ),
                              ),
                              TextButton(
                                onPressed: () {
                                  Navigator.pushReplacement(
                                    context,
                                    MaterialPageRoute(
                                      builder: (context) => const LoginScreen(),
                                    ),
                                  );
                                },
                                child: Text(
                                  'Войти',
                                  style: TextStyle(
                                    fontWeight: FontWeight.bold,
                                    fontSize: isSmallScreen ? 14.0 : null,
                                  ),
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
} 