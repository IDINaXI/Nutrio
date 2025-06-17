import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../models/day_meal_plan.dart';
import '../models/meal.dart';
import '../services/api_service.dart';
import 'day_meal_plan_history_screen.dart';

class DayMealPlanScreen extends StatefulWidget {
  const DayMealPlanScreen({Key? key}) : super(key: key);

  @override
  State<DayMealPlanScreen> createState() => _DayMealPlanScreenState();
}

class _DayMealPlanScreenState extends State<DayMealPlanScreen> {
  DayMealPlan? _plan;
  bool _loading = false;
  String? _error;

  Future<void> _generatePlan() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final plan = await ApiService().generateDayMealPlan();
      setState(() => _plan = plan);
    } catch (e) {
      setState(() => _error = e.toString());
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  void initState() {
    super.initState();
    _generatePlan();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('План питания на день'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loading ? null : _generatePlan,
          ),
          IconButton(
            icon: const Icon(Icons.history),
            onPressed: () {
              Navigator.of(context).push(MaterialPageRoute(
                builder: (_) => const DayMealPlanHistoryScreen(),
              ));
            },
          ),
        ],
      ),
      body: _loading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(child: Text(_error!, style: const TextStyle(color: Colors.red)))
              : _plan == null
                  ? const Center(child: Text('Нет данных'))
                  : ListView(
                      padding: const EdgeInsets.all(16),
                      children: [
                        _MealCard(title: 'Завтрак', meal: _plan!.breakfast),
                        _MealCard(title: 'Обед', meal: _plan!.lunch),
                        _MealCard(title: 'Ужин', meal: _plan!.dinner),
                        _MealCard(title: 'Перекус', meal: _plan!.snack),
                        const SizedBox(height: 16),
                        Text('Калорий за день: ${_plan!.totalCalories.toStringAsFixed(1)}'),
                        Text('Б: ${_plan!.macronutrients['protein']?.toStringAsFixed(1) ?? '0'} г, Ж: ${_plan!.macronutrients['fat']?.toStringAsFixed(1) ?? '0'} г, У: ${_plan!.macronutrients['carbs']?.toStringAsFixed(1) ?? '0'} г'),
                        Text('Дата: ${DateFormat('dd.MM.yyyy').format(DateTime.parse(_plan!.date))}'),
                      ],
                    ),
    );
  }
}

class _MealCard extends StatelessWidget {
  final String title;
  final Meal meal;

  const _MealCard({required this.title, required this.meal});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(vertical: 8),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleMedium),
            Text(meal.name, style: const TextStyle(fontWeight: FontWeight.bold)),
            Text('Калории: ${meal.calories.toStringAsFixed(1)}'),
            Text('Б: ${meal.macros['protein']?.toStringAsFixed(1) ?? '0'} г, Ж: ${meal.macros['fat']?.toStringAsFixed(1) ?? '0'} г, У: ${meal.macros['carbs']?.toStringAsFixed(1) ?? '0'} г'),
            Text('Ингредиенты: ${meal.ingredients.join(', ')}'),
            Text('Рецепт: ${meal.recipe}'),
            if (meal.imageUrl != null)
              Image.network(
                meal.imageUrl!,
                height: 200,
                width: double.infinity,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) => const SizedBox.shrink(),
              ),
          ],
        ),
      ),
    );
  }
} 