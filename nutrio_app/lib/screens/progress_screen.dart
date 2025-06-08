import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'dart:math';

class ProgressScreen extends StatefulWidget {
  const ProgressScreen({super.key});

  @override
  State<ProgressScreen> createState() => _ProgressScreenState();
}

class _ProgressScreenState extends State<ProgressScreen> {
  // Мок-данные: список записей веса
  List<_WeightEntry> _weights = [
    _WeightEntry(DateTime.now().subtract(const Duration(days: 6)), 82),
    _WeightEntry(DateTime.now().subtract(const Duration(days: 5)), 81.5),
    _WeightEntry(DateTime.now().subtract(const Duration(days: 4)), 81.2),
    _WeightEntry(DateTime.now().subtract(const Duration(days: 3)), 80.8),
    _WeightEntry(DateTime.now().subtract(const Duration(days: 2)), 80.5),
    _WeightEntry(DateTime.now().subtract(const Duration(days: 1)), 80.2),
    _WeightEntry(DateTime.now(), 80),
  ];

  void _addWeight() async {
    final controller = TextEditingController();
    final result = await showDialog<double>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Добавить вес'),
        content: TextField(
          controller: controller,
          keyboardType: TextInputType.numberWithOptions(decimal: true),
          decoration: const InputDecoration(labelText: 'Вес (кг)'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Отмена'),
          ),
          ElevatedButton(
            onPressed: () {
              final value = double.tryParse(controller.text.replaceAll(',', '.'));
              if (value != null) {
                Navigator.pop(context, value);
              }
            },
            child: const Text('Добавить'),
          ),
        ],
      ),
    );
    if (result != null) {
      setState(() {
        _weights.add(_WeightEntry(DateTime.now(), result));
        _weights.sort((a, b) => a.date.compareTo(b.date));
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final mainGreen = Theme.of(context).colorScheme.primary;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Прогресс'),
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
          child: Padding(
            padding: const EdgeInsets.all(24.0),
            child: Card(
              elevation: 8,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(28)),
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 24),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          'График веса',
                          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: mainGreen,
                          ),
                        ),
                        IconButton(
                          icon: const Icon(Icons.add),
                          color: mainGreen,
                          tooltip: 'Добавить вес',
                          onPressed: _addWeight,
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    Expanded(
                      child: _weights.length < 2
                          ? Center(
                              child: Text(
                                'Недостаточно данных для графика',
                                style: Theme.of(context).textTheme.bodyLarge,
                              ),
                            )
                          : LineChart(
                              LineChartData(
                                minY: _weights.map((e) => e.weight).reduce(min) - 1,
                                maxY: _weights.map((e) => e.weight).reduce(max) + 1,
                                titlesData: FlTitlesData(
                                  leftTitles: AxisTitles(
                                    sideTitles: SideTitles(showTitles: true, reservedSize: 36),
                                  ),
                                  bottomTitles: AxisTitles(
                                    sideTitles: SideTitles(
                                      showTitles: true,
                                      getTitlesWidget: (value, meta) {
                                        final idx = value.toInt();
                                        if (idx < 0 || idx >= _weights.length) return const SizedBox();
                                        final date = _weights[idx].date;
                                        return Text('${date.day}.${date.month}', style: const TextStyle(fontSize: 12));
                                      },
                                      reservedSize: 32,
                                    ),
                                  ),
                                  rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
                                  topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
                                ),
                                gridData: FlGridData(show: true, drawVerticalLine: false),
                                borderData: FlBorderData(show: false),
                                lineBarsData: [
                                  LineChartBarData(
                                    spots: [
                                      for (int i = 0; i < _weights.length; i++)
                                        FlSpot(i.toDouble(), _weights[i].weight),
                                    ],
                                    isCurved: true,
                                    color: mainGreen,
                                    barWidth: 4,
                                    dotData: FlDotData(show: true),
                                    belowBarData: BarAreaData(
                                      show: true,
                                      color: mainGreen.withOpacity(0.12),
                                    ),
                                  ),
                                ],
                              ),
                            ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _WeightStat(label: 'Начальный', value: _weights.first.weight),
                        _WeightStat(label: 'Текущий', value: _weights.last.weight),
                        _WeightStat(label: 'Изменение', value: _weights.last.weight - _weights.first.weight),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _WeightEntry {
  final DateTime date;
  final double weight;
  _WeightEntry(this.date, this.weight);
}

class _WeightStat extends StatelessWidget {
  final String label;
  final double value;
  const _WeightStat({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final mainGreen = Theme.of(context).colorScheme.primary;
    return Column(
      children: [
        Text(label, style: TextStyle(color: Colors.grey[600], fontSize: 13)),
        const SizedBox(height: 4),
        Text(
          value.toStringAsFixed(1) + ' кг',
          style: TextStyle(fontWeight: FontWeight.bold, color: mainGreen, fontSize: 16),
        ),
      ],
    );
  }
} 