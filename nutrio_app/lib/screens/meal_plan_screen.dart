import 'package:flutter/material.dart';
import '../models/meal_plan.dart';
import '../models/auth_user.dart';
import '../services/api_service.dart';
import '../services/auth_service.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'dart:convert';

class MealPlanScreen extends StatefulWidget {
  final AuthUser user;

  const MealPlanScreen({super.key, required this.user});

  @override
  State<MealPlanScreen> createState() => _MealPlanScreenState();
}

class _MealPlanScreenState extends State<MealPlanScreen> {
  MealPlan? _mealPlan;
  bool _isLoading = false;
  String? _error;
  int _selectedDayIndex = DateTime.now().weekday - 1; // 0=Пн, 6=Вс
  final ApiService _apiService = ApiService();
  final AuthService _authService = AuthService();

  static const List<String> weekDays = [
    'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'
  ];

  @override
  void initState() {
    super.initState();
    _isLoading = true;
    WidgetsBinding.instance.addPostFrameCallback((_) => _loadMealPlanFromServer());
  }

  Future<void> _loadMealPlanFromServer() async {
    setState(() => _isLoading = true);
    try {
      final plan = await _apiService.fetchSavedMealPlan(widget.user.id);
      setState(() {
        _mealPlan = plan;
        _isLoading = false;
        _error = plan == null ? 'План питания еще не сгенерирован' : null;
        // Если план есть, выставить выбранный день на сегодня, если он есть в week
        if (plan != null && plan.week.isNotEmpty) {
          final today = weekDays[DateTime.now().weekday - 1];
          final idx = plan.week.indexWhere((d) => d.day == today);
          if (idx != -1) _selectedDayIndex = idx;
        }
      });
    } catch (e) {
      setState(() {
        _isLoading = false;
        _error = 'Ошибка загрузки плана питания: $e';
      });
    }
  }

  Future<void> _generateMealPlan() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      if (widget.user == null) {
        throw Exception('Пожалуйста, войдите снова, чтобы сгенерировать план питания');
      }

      print('Генерация плана питания для пользователя: ${widget.user?.name}');
      print('Данные пользователя: возраст=${widget.user?.age}, вес=${widget.user?.weight}, рост=${widget.user?.height}, активность=${widget.user?.activityLevel}, цель=${widget.user?.goal}');

      final mealPlan = await _apiService.generateMealPlan(widget.user!);
      setState(() {
        _mealPlan = mealPlan;
        _isLoading = false;
      });
      await _apiService.saveMealPlan(widget.user.id, mealPlan);
    } catch (e) {
      print('Ошибка генерации плана питания: $e');
      setState(() {
        _isLoading = false;
        _error = e.toString().replaceAll('Exception: ', '');
      });
    }
  }

  Future<void> _regenerateDay(String day) async {
    if (_mealPlan == null) return;

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final regeneratedDay = await _apiService.regenerateDay(
        widget.user.id,
        day,
        _mealPlan!,
      );

      setState(() {
        // Обновляем день в текущем плане
        final dayIndex = _mealPlan!.week.indexWhere((d) => d.day == day);
        if (dayIndex != -1) {
          // Конвертируем DayMealPlan в WeekDayPlan с правильной конвертацией Meal
          final weekDayPlan = WeekDayPlan(
            day: day,
            breakfast: Meal(
              name: regeneratedDay.breakfast.name,
              description: regeneratedDay.breakfast.name,
              calories: regeneratedDay.breakfast.calories,
              macros: regeneratedDay.breakfast.macros,
              ingredients: regeneratedDay.breakfast.ingredients,
              recipe: regeneratedDay.breakfast.recipe,
            ),
            lunch: Meal(
              name: regeneratedDay.lunch.name,
              description: regeneratedDay.lunch.name,
              calories: regeneratedDay.lunch.calories,
              macros: regeneratedDay.lunch.macros,
              ingredients: regeneratedDay.lunch.ingredients,
              recipe: regeneratedDay.lunch.recipe,
            ),
            dinner: Meal(
              name: regeneratedDay.dinner.name,
              description: regeneratedDay.dinner.name,
              calories: regeneratedDay.dinner.calories,
              macros: regeneratedDay.dinner.macros,
              ingredients: regeneratedDay.dinner.ingredients,
              recipe: regeneratedDay.dinner.recipe,
            ),
            snack: Meal(
              name: regeneratedDay.snack.name,
              description: regeneratedDay.snack.name,
              calories: regeneratedDay.snack.calories,
              macros: regeneratedDay.snack.macros,
              ingredients: regeneratedDay.snack.ingredients,
              recipe: regeneratedDay.snack.recipe,
            ),
            totalCalories: regeneratedDay.totalCalories,
            macronutrients: regeneratedDay.macronutrients,
          );
          _mealPlan!.week[dayIndex] = weekDayPlan;
        }
        _isLoading = false;
      });

      // Сохраняем обновленный план
      await _apiService.saveMealPlan(widget.user.id, _mealPlan!);
    } catch (e) {
      print('Ошибка перегенерации дня: $e');
      setState(() {
        _isLoading = false;
        _error = e.toString().replaceAll('Exception: ', '');
      });
    }
  }

  void _onDaySelected(int idx) {
    setState(() => _selectedDayIndex = idx);
  }

  @override
  Widget build(BuildContext context) {
    final mainGreen = Theme.of(context).colorScheme.primary;
    return Scaffold(
      extendBodyBehindAppBar: false,
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: Row(
          children: [
            Icon(Icons.restaurant_menu, color: mainGreen),
            const SizedBox(width: 8),
            const Text('План питания'),
          ],
        ),
        backgroundColor: Colors.white.withOpacity(0.95),
        foregroundColor: mainGreen,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _isLoading ? null : _generateMealPlan,
          ),
        ],
        shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(bottom: Radius.circular(24)),
        ),
      ),
      body: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            colors: [mainGreen.withOpacity(0.04), Colors.white],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: _isLoading
            ? const Center(child: CircularProgressIndicator())
            : _error != null
                ? Center(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.error_outline,
                            size: 48,
                            color: Theme.of(context).colorScheme.error,
                          ),
                          const SizedBox(height: 16),
                          Text(
                            _error!,
                            textAlign: TextAlign.center,
                            style: Theme.of(context).textTheme.bodyLarge,
                          ),
                          const SizedBox(height: 16),
                          ElevatedButton(
                            style: ElevatedButton.styleFrom(
                              backgroundColor: mainGreen,
                              foregroundColor: Colors.white,
                              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                            ),
                            onPressed: _generateMealPlan,
                            child: const Text('Сгенерировать план питания', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                          ),
                        ],
                      ),
                    ),
                  )
                : _mealPlan == null || _mealPlan!.week.isEmpty
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.restaurant_menu, size: 56, color: mainGreen),
                            const SizedBox(height: 16),
                            const Text('План питания еще не сгенерирован', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              style: ElevatedButton.styleFrom(
                                backgroundColor: mainGreen,
                                foregroundColor: Colors.white,
                                padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
                                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                              ),
                              onPressed: _generateMealPlan,
                              child: const Text('Сгенерировать план питания', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                            ),
                          ],
                        ),
                      )
                    : Column(
                        children: [
                          const SizedBox(height: 16), // отступ чтобы не залезало под AppBar
                          SizedBox(
                            height: 56,
                            child: ListView.separated(
                              scrollDirection: Axis.horizontal,
                              padding: const EdgeInsets.symmetric(horizontal: 16),
                              itemCount: _mealPlan!.week.length,
                              separatorBuilder: (_, __) => const SizedBox(width: 10),
                              itemBuilder: (context, i) {
                                final isSelected = i == _selectedDayIndex;
                                final dayLabel = _mealPlan!.week[i].day;
                                return GestureDetector(
                                  onTap: () => _onDaySelected(i),
                                  child: AnimatedContainer(
                                    duration: const Duration(milliseconds: 200),
                                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
                                    decoration: BoxDecoration(
                                      color: isSelected ? mainGreen : Colors.white,
                                      borderRadius: BorderRadius.circular(16),
                                      border: Border.all(color: mainGreen.withOpacity(0.18)),
                                      boxShadow: isSelected
                                          ? [BoxShadow(color: mainGreen.withOpacity(0.12), blurRadius: 12, offset: Offset(0, 4))]
                                          : [],
                                    ),
                                    child: Row(
                                      mainAxisSize: MainAxisSize.min,
                                      children: [
                                        Text(
                                          dayLabel,
                                          style: TextStyle(
                                            color: isSelected ? Colors.white : mainGreen,
                                            fontWeight: FontWeight.bold,
                                            fontSize: 17,
                                            letterSpacing: 0.5,
                                          ),
                                        ),
                                        const SizedBox(width: 8),
                                        IconButton(
                                          icon: Icon(
                                            Icons.refresh,
                                            color: isSelected ? Colors.white : mainGreen,
                                            size: 20,
                                          ),
                                          onPressed: () => _regenerateDay(dayLabel),
                                          padding: EdgeInsets.zero,
                                          constraints: const BoxConstraints(),
                                        ),
                                      ],
                                    ),
                                  ),
                                );
                              },
                            ),
                          ),
                          const SizedBox(height: 20),
                          Expanded(
                            child: ListView(
                              padding: const EdgeInsets.symmetric(horizontal: 0, vertical: 8),
                              children: [
                                Card(
                                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                                  elevation: 6,
                                  margin: const EdgeInsets.symmetric(vertical: 8, horizontal: 8),
                                  color: Colors.white,
                                  child: Padding(
                                    padding: const EdgeInsets.all(20.0),
                                    child: Column(
                                      crossAxisAlignment: CrossAxisAlignment.start,
                                      children: [
                                        _PrettyMealSection(
                                          title: 'Завтрак',
                                          icon: Icons.wb_sunny,
                                          color: mainGreen.withOpacity(0.08),
                                          meal: _mealPlan!.week[_selectedDayIndex].breakfast,
                                          iconColor: mainGreen,
                                        ),
                                        _PrettyMealSection(
                                          title: 'Обед',
                                          icon: Icons.lunch_dining,
                                          color: mainGreen.withOpacity(0.08),
                                          meal: _mealPlan!.week[_selectedDayIndex].lunch,
                                          iconColor: mainGreen,
                                        ),
                                        _PrettyMealSection(
                                          title: 'Ужин',
                                          icon: Icons.nightlight_round,
                                          color: mainGreen.withOpacity(0.08),
                                          meal: _mealPlan!.week[_selectedDayIndex].dinner,
                                          iconColor: mainGreen,
                                        ),
                                        _PrettyMealSection(
                                          title: 'Перекус',
                                          icon: Icons.emoji_food_beverage,
                                          color: mainGreen.withOpacity(0.08),
                                          meal: _mealPlan!.week[_selectedDayIndex].snack,
                                          iconColor: mainGreen,
                                        ),
                                        const SizedBox(height: 18),
                                        Row(
                                          mainAxisAlignment: MainAxisAlignment.spaceAround,
                                          children: [
                                            _MacroChip(label: 'Ккал', value: _mealPlan!.week[_selectedDayIndex].totalCalories.toStringAsFixed(0), color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                            _MacroChip(label: 'Б', value: _mealPlan!.week[_selectedDayIndex].macronutrients['proteins']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                            _MacroChip(label: 'Ж', value: _mealPlan!.week[_selectedDayIndex].macronutrients['fats']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                            _MacroChip(label: 'У', value: _mealPlan!.week[_selectedDayIndex].macronutrients['carbs']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                          ],
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                                const SizedBox(height: 24),
                                Center(
                                  child: ElevatedButton.icon(
                                    icon: const Icon(Icons.calendar_month),
                                    label: const Text('Показать всю неделю'),
                                    style: ElevatedButton.styleFrom(
                                      backgroundColor: mainGreen,
                                      foregroundColor: Colors.white,
                                      padding: const EdgeInsets.symmetric(horizontal: 28, vertical: 16),
                                      textStyle: const TextStyle(fontSize: 17, fontWeight: FontWeight.bold),
                                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                                      elevation: 3,
                                    ),
                                    onPressed: () {
                                      showModalBottomSheet(
                                        context: context,
                                        isScrollControlled: true,
                                        backgroundColor: Colors.transparent,
                                        builder: (_) => DraggableScrollableSheet(
                                          expand: false,
                                          initialChildSize: 0.92,
                                          minChildSize: 0.5,
                                          maxChildSize: 0.98,
                                          builder: (context, scrollController) {
                                            return Container(
                                              decoration: const BoxDecoration(
                                                color: Colors.white,
                                                borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
                                              ),
                                              child: ListView.builder(
                                                controller: scrollController,
                                                itemCount: _mealPlan!.week.length,
                                                itemBuilder: (context, i) {
                                                  final dayPlan = _mealPlan!.week[i];
                                                  return Column(
                                                    crossAxisAlignment: CrossAxisAlignment.start,
                                                    children: [
                                                      Padding(
                                                        padding: const EdgeInsets.only(left: 8, bottom: 4, top: 8),
                                                        child: Text(
                                                          dayPlan.day,
                                                          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                                            fontWeight: FontWeight.bold,
                                                            color: mainGreen,
                                                            fontSize: 22,
                                                            letterSpacing: 1.2,
                                                          ),
                                                        ),
                                                      ),
                                                      Card(
                                                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                                                        elevation: 6,
                                                        margin: const EdgeInsets.symmetric(vertical: 4),
                                                        color: Colors.white,
                                                        child: Padding(
                                                          padding: const EdgeInsets.all(20.0),
                                                          child: Column(
                                                            crossAxisAlignment: CrossAxisAlignment.start,
                                                            children: [
                                                              _PrettyMealSection(title: 'Завтрак', icon: Icons.wb_sunny, color: mainGreen.withOpacity(0.08), meal: dayPlan.breakfast, iconColor: mainGreen),
                                                              _PrettyMealSection(title: 'Обед', icon: Icons.lunch_dining, color: mainGreen.withOpacity(0.08), meal: dayPlan.lunch, iconColor: mainGreen),
                                                              _PrettyMealSection(title: 'Ужин', icon: Icons.nightlight_round, color: mainGreen.withOpacity(0.08), meal: dayPlan.dinner, iconColor: mainGreen),
                                                              _PrettyMealSection(title: 'Перекус', icon: Icons.emoji_food_beverage, color: mainGreen.withOpacity(0.08), meal: dayPlan.snack, iconColor: mainGreen),
                                                              const SizedBox(height: 12),
                                                              Row(
                                                                mainAxisAlignment: MainAxisAlignment.spaceAround,
                                                                children: [
                                                                  _MacroChip(label: 'Ккал', value: dayPlan.totalCalories.toStringAsFixed(0), color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                                                  _MacroChip(label: 'Б', value: dayPlan.macronutrients['proteins']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                                                  _MacroChip(label: 'Ж', value: dayPlan.macronutrients['fats']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                                                  _MacroChip(label: 'У', value: dayPlan.macronutrients['carbs']?.toStringAsFixed(0) ?? '0', color: mainGreen.withOpacity(0.12), textColor: mainGreen),
                                                                ],
                                                              ),
                                                            ],
                                                          ),
                                                        ),
                                                      ),
                                                      const SizedBox(height: 16),
                                                    ],
                                                  );
                                                },
                                              ),
                                            );
                                          },
                                        ),
                                      );
                                    },
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
      ),
    );
  }
}

class _PrettyMealSection extends StatelessWidget {
  final String title;
  final IconData icon;
  final Color color;
  final Meal meal;
  final Color iconColor;
  const _PrettyMealSection({required this.title, required this.icon, required this.color, required this.meal, this.iconColor = Colors.green});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 6),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(14),
      ),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, size: 32, color: iconColor),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(meal.name, style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                const SizedBox(height: 4),
                Row(
                  children: [
                    _MacroChip(label: 'Ккал', value: meal.calories.toStringAsFixed(0), color: iconColor.withOpacity(0.12), textColor: iconColor),
                    const SizedBox(width: 6),
                    _MacroChip(label: 'Б', value: meal.macros['protein']?.toStringAsFixed(0) ?? '0', color: iconColor.withOpacity(0.12), textColor: iconColor),
                    const SizedBox(width: 6),
                    _MacroChip(label: 'Ж', value: meal.macros['fat']?.toStringAsFixed(0) ?? '0', color: iconColor.withOpacity(0.12), textColor: iconColor),
                    const SizedBox(width: 6),
                    _MacroChip(label: 'У', value: meal.macros['carbs']?.toStringAsFixed(0) ?? '0', color: iconColor.withOpacity(0.12), textColor: iconColor),
                  ],
                ),
                if (meal.ingredients.isNotEmpty)
                  Padding(
                    padding: const EdgeInsets.only(top: 4.0),
                    child: Text('Ингредиенты: ${meal.ingredients.join(', ')}', style: Theme.of(context).textTheme.bodySmall),
                  ),
                if (meal.recipe.isNotEmpty)
                  Container(
                    margin: const EdgeInsets.only(top: 6),
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(8),
                      border: Border.all(color: Colors.grey[300]!),
                    ),
                    child: Text(meal.recipe, style: Theme.of(context).textTheme.bodySmall?.copyWith(fontStyle: FontStyle.italic)),
                  ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _MacroChip extends StatelessWidget {
  final String label;
  final String value;
  final Color color;
  final Color textColor;
  const _MacroChip({required this.label, required this.value, required this.color, this.textColor = Colors.green});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text('$label: $value', style: TextStyle(fontWeight: FontWeight.bold, color: textColor)),
    );
  }
} 