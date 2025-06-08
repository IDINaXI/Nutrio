import 'package:flutter/material.dart';
import '../models/day_meal_plan.dart';
import '../services/api_service.dart';

class DayMealPlanHistoryScreen extends StatefulWidget {
  const DayMealPlanHistoryScreen({Key? key}) : super(key: key);

  @override
  State<DayMealPlanHistoryScreen> createState() => _DayMealPlanHistoryScreenState();
}

class _DayMealPlanHistoryScreenState extends State<DayMealPlanHistoryScreen> {
  late Future<List<DayMealPlan>> _future;

  @override
  void initState() {
    super.initState();
    _future = ApiService().getDayMealPlanHistory();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('История дневных планов')),
      body: FutureBuilder<List<DayMealPlan>>(
        future: _future,
        builder: (context, snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) {
            return Center(child: Text('Ошибка: ${snapshot.error}'));
          }
          final plans = snapshot.data ?? [];
          if (plans.isEmpty) {
            return const Center(child: Text('История пуста'));
          }
          return ListView.builder(
            itemCount: plans.length,
            itemBuilder: (context, i) {
              final plan = plans[i];
              return ListTile(
                title: Text('План на ${plan.date}'),
                subtitle: Text('Калорий: ${plan.totalCalories}'),
                onTap: () {
                  // Можно сделать переход на подробный просмотр плана
                },
              );
            },
          );
        },
      ),
    );
  }
} 