import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../models/weight_entry.dart';
import '../services/weight_service.dart';

class WeightTrackingScreen extends StatefulWidget {
  final int userId;

  const WeightTrackingScreen({Key? key, required this.userId}) : super(key: key);

  @override
  _WeightTrackingScreenState createState() => _WeightTrackingScreenState();
}

class _WeightTrackingScreenState extends State<WeightTrackingScreen> {
  final WeightService _weightService = WeightService();
  List<WeightEntry> _weightEntries = [];
  bool _isLoading = true;
  final _formKey = GlobalKey<FormState>();
  final _weightController = TextEditingController();
  DateTime _selectedDate = DateTime.now();

  @override
  void initState() {
    super.initState();
    _loadWeightData();
  }

  Future<void> _loadWeightData() async {
    try {
      final entries = await _weightService.getUserWeightHistory(widget.userId);
      setState(() {
        _weightEntries = entries;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Failed to load weight data: $e')),
      );
    }
  }

  Future<void> _addWeightEntry() async {
    if (_formKey.currentState!.validate()) {
      try {
        await _weightService.addWeightEntry(
          widget.userId,
          double.parse(_weightController.text),
          _selectedDate,
        );
        _weightController.clear();
        _loadWeightData();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Weight entry added successfully')),
        );
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Failed to add weight entry: $e')),
        );
      }
    }
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime.now(),
      locale: const Locale('ru', 'RU'),
    );
    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Отслеживание веса'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Form(
                        key: _formKey,
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            TextFormField(
                              controller: _weightController,
                              decoration: const InputDecoration(
                                labelText: 'Вес (кг)',
                                border: OutlineInputBorder(),
                              ),
                              keyboardType: TextInputType.number,
                              validator: (value) {
                                if (value == null || value.isEmpty) {
                                  return 'Пожалуйста, введите ваш вес';
                                }
                                if (double.tryParse(value) == null) {
                                  return 'Введите корректное число';
                                }
                                return null;
                              },
                            ),
                            const SizedBox(height: 16),
                            ListTile(
                              title: const Text('Дата'),
                              subtitle: Text(
                                '${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}',
                              ),
                              trailing: const Icon(Icons.calendar_today),
                              onTap: () => _selectDate(context),
                            ),
                            const SizedBox(height: 16),
                            ElevatedButton(
                              onPressed: _addWeightEntry,
                              child: const Text('Добавить запись'),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  Card(
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.stretch,
                        children: [
                          const Text(
                            'История веса',
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(height: 16),
                          SizedBox(
                            height: 300,
                            child: _weightEntries.isEmpty
                                ? const Center(
                                    child: Text('Нет записей веса'),
                                  )
                                : LineChart(
                                    LineChartData(
                                      gridData: FlGridData(show: true),
                                      titlesData: FlTitlesData(
                                        leftTitles: AxisTitles(
                                          sideTitles: SideTitles(
                                            showTitles: true,
                                            reservedSize: 40,
                                          ),
                                        ),
                                        bottomTitles: AxisTitles(
                                          sideTitles: SideTitles(
                                            showTitles: true,
                                            getTitlesWidget: (value, meta) {
                                              if (value.toInt() >= 0 &&
                                                  value.toInt() <
                                                      _weightEntries.length) {
                                                final date =
                                                    _weightEntries[value.toInt()]
                                                        .date;
                                                return Text(
                                                  '${date.day}/${date.month}',
                                                  style: const TextStyle(
                                                    fontSize: 10,
                                                  ),
                                                );
                                              }
                                              return const Text('');
                                            },
                                          ),
                                        ),
                                        rightTitles: AxisTitles(
                                          sideTitles: SideTitles(showTitles: false),
                                        ),
                                        topTitles: AxisTitles(
                                          sideTitles: SideTitles(showTitles: false),
                                        ),
                                      ),
                                      borderData: FlBorderData(show: true),
                                      lineBarsData: [
                                        LineChartBarData(
                                          spots: _weightEntries
                                              .asMap()
                                              .entries
                                              .map((entry) {
                                            return FlSpot(
                                              entry.key.toDouble(),
                                              entry.value.weight,
                                            );
                                          }).toList(),
                                          isCurved: true,
                                          color: Colors.blue,
                                          barWidth: 3,
                                          dotData: FlDotData(show: true),
                                        ),
                                      ],
                                    ),
                                  ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
    );
  }

  @override
  void dispose() {
    _weightController.dispose();
    super.dispose();
  }
} 