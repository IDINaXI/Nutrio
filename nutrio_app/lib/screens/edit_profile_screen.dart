import 'package:flutter/material.dart';
import '../models/auth_user.dart';
import '../services/auth_service.dart';

class EditProfileScreen extends StatefulWidget {
  final AuthUser user;

  const EditProfileScreen({super.key, required this.user});

  @override
  State<EditProfileScreen> createState() => _EditProfileScreenState();
}

class _EditProfileScreenState extends State<EditProfileScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nameController;
  late TextEditingController _emailController;
  late TextEditingController _ageController;
  late TextEditingController _heightController;
  late TextEditingController _weightController;
  late TextEditingController _allergiesController;
  String _selectedGender = 'MALE';
  String _selectedGoal = 'MAINTAIN_WEIGHT';
  String _selectedActivityLevel = 'MODERATELY_ACTIVE';
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _nameController = TextEditingController(text: widget.user.name);
    _emailController = TextEditingController(text: widget.user.email);
    _ageController = TextEditingController(text: widget.user.age.toString());
    _heightController = TextEditingController(text: widget.user.height.toString());
    _weightController = TextEditingController(text: widget.user.weight.toString());
    _allergiesController = TextEditingController(text: widget.user.allergies.join(', '));
    _selectedGender = widget.user.gender;
    _selectedGoal = widget.user.goal;
    _selectedActivityLevel = widget.user.activityLevel;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _ageController.dispose();
    _heightController.dispose();
    _weightController.dispose();
    _allergiesController.dispose();
    super.dispose();
  }

  Future<void> _updateProfile() async {
    if (_formKey.currentState!.validate()) {
      setState(() => _isLoading = true);
      try {
        await AuthService().updateProfile(
          name: _nameController.text,
          age: int.parse(_ageController.text),
          height: double.parse(_heightController.text),
          weight: double.parse(_weightController.text),
          gender: _selectedGender,
          goal: _selectedGoal,
          activityLevel: _selectedActivityLevel,
          allergies: _allergiesController.text.split(',').map((e) => e.trim()).where((e) => e.isNotEmpty).toList(),
          email: _emailController.text,
        );
        if (mounted) {
          Navigator.pop(context);
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
    final mainGreen = Theme.of(context).colorScheme.primary;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Редактировать профиль'),
        backgroundColor: Colors.white.withOpacity(0.95),
        foregroundColor: mainGreen,
        elevation: 0,
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(bottom: Radius.circular(24)),
        ),
      ),
      backgroundColor: Colors.transparent,
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [mainGreen.withOpacity(0.04), Colors.white],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              child: Padding(
                padding: const EdgeInsets.all(24.0),
                child: Card(
                  elevation: 8,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
                  child: Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 36),
                    child: Form(
                      key: _formKey,
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          TextFormField(
                            controller: _nameController,
                            decoration: const InputDecoration(labelText: 'Имя', prefixIcon: Icon(Icons.person_outline)),
                            validator: (value) => value == null || value.isEmpty ? 'Пожалуйста, введите ваше имя' : null,
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _emailController,
                            decoration: const InputDecoration(labelText: 'Электронная почта', prefixIcon: Icon(Icons.email_outlined)),
                            validator: (value) {
                              if (value == null || value.isEmpty) return 'Пожалуйста, введите вашу электронную почту';
                              if (!value.contains('@')) return 'Пожалуйста, введите действительный электронный адрес';
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _ageController,
                            decoration: const InputDecoration(labelText: 'Возраст', prefixIcon: Icon(Icons.cake_outlined)),
                            keyboardType: TextInputType.number,
                            validator: (value) {
                              if (value == null || value.isEmpty) return 'Пожалуйста, введите ваш возраст';
                              if (int.tryParse(value) == null) return 'Пожалуйста, введите действительный возраст';
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _heightController,
                            decoration: const InputDecoration(labelText: 'Рост (см)', prefixIcon: Icon(Icons.height)),
                            keyboardType: TextInputType.number,
                            validator: (value) {
                              if (value == null || value.isEmpty) return 'Пожалуйста, введите ваш рост';
                              if (double.tryParse(value) == null) return 'Пожалуйста, введите действительный рост';
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _weightController,
                            decoration: const InputDecoration(labelText: 'Вес (кг)', prefixIcon: Icon(Icons.monitor_weight_outlined)),
                            keyboardType: TextInputType.number,
                            validator: (value) {
                              if (value == null || value.isEmpty) return 'Пожалуйста, введите ваш вес';
                              if (double.tryParse(value) == null) return 'Пожалуйста, введите действительный вес';
                              return null;
                            },
                          ),
                          const SizedBox(height: 16),
                          DropdownButtonFormField<String>(
                            value: _selectedGender,
                            decoration: const InputDecoration(labelText: 'Пол', prefixIcon: Icon(Icons.wc)),
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
                            decoration: const InputDecoration(labelText: 'Цель', prefixIcon: Icon(Icons.flag_outlined)),
                            items: const [
                              DropdownMenuItem(value: 'LOSE_WEIGHT', child: Text('Сбросить вес')),
                              DropdownMenuItem(value: 'MAINTAIN_WEIGHT', child: Text('Сохранить вес')),
                              DropdownMenuItem(value: 'GAIN_WEIGHT', child: Text('Набрать вес')),
                            ],
                            onChanged: (value) {
                              setState(() => _selectedGoal = value!);
                            },
                          ),
                          const SizedBox(height: 16),
                          DropdownButtonFormField<String>(
                            value: _selectedActivityLevel,
                            decoration: const InputDecoration(labelText: 'Уровень активности', prefixIcon: Icon(Icons.directions_run)),
                            items: const [
                              DropdownMenuItem(value: 'SEDENTARY', child: Text('Сидячий')),
                              DropdownMenuItem(value: 'LIGHTLY_ACTIVE', child: Text('Слабоактивный')),
                              DropdownMenuItem(value: 'MODERATELY_ACTIVE', child: Text('Умеренно активный')),
                              DropdownMenuItem(value: 'VERY_ACTIVE', child: Text('Очень активный')),
                              DropdownMenuItem(value: 'EXTREMELY_ACTIVE', child: Text('Экстремально активный')),
                            ],
                            onChanged: (value) {
                              setState(() => _selectedActivityLevel = value!);
                            },
                          ),
                          const SizedBox(height: 16),
                          TextFormField(
                            controller: _allergiesController,
                            decoration: const InputDecoration(
                              labelText: 'Аллергии и продукты, которые не нравятся',
                              hintText: 'Например: орехи, молоко, яйца, рыба',
                              prefixIcon: Icon(Icons.warning_amber_outlined),
                            ),
                          ),
                          const SizedBox(height: 28),
                          SizedBox(
                            width: double.infinity,
                            child: ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: mainGreen,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(vertical: 18),
                                shape: RoundedRectangleBorder(
                                  borderRadius: BorderRadius.circular(16),
                                ),
                                elevation: 2,
                              ),
                              onPressed: _isLoading ? null : _updateProfile,
                              child: _isLoading
                                  ? const SizedBox(height: 20, width: 20, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                                  : const Text('Сохранить изменения', style: TextStyle(fontSize: 18, fontWeight: FontWeight.w600)),
                            ),
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