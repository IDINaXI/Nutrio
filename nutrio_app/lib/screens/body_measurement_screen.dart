import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import '../models/body_measurement.dart';
import '../services/body_measurement_service.dart';

class BodyMeasurementScreen extends StatefulWidget {
  final int userId;
  const BodyMeasurementScreen({Key? key, required this.userId}) : super(key: key);

  @override
  State<BodyMeasurementScreen> createState() => _BodyMeasurementScreenState();
}

class _BodyMeasurementScreenState extends State<BodyMeasurementScreen> {
  final BodyMeasurementService _service = BodyMeasurementService();
  final _formKey = GlobalKey<FormState>();
  final _waistController = TextEditingController();
  final _chestController = TextEditingController();
  final _hipsController = TextEditingController();
  final _armController = TextEditingController();
  final _legController = TextEditingController();
  DateTime _selectedDate = DateTime.now();
  List<BodyMeasurement> _measurements = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadMeasurements();
  }

  Future<void> _loadMeasurements() async {
    setState(() => _isLoading = true);
    try {
      final data = await _service.getUserMeasurements(widget.userId);
      setState(() {
        _measurements = data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Ошибка загрузки замеров: $e')),
      );
    }
  }

  Future<void> _addMeasurement() async {
    if (_formKey.currentState!.validate()) {
      try {
        await _service.addMeasurement(
          BodyMeasurement(
            id: null,
            userId: widget.userId,
            date: _selectedDate,
            waist: _waistController.text.isNotEmpty ? double.parse(_waistController.text) : null,
            chest: _chestController.text.isNotEmpty ? double.parse(_chestController.text) : null,
            hips: _hipsController.text.isNotEmpty ? double.parse(_hipsController.text) : null,
            arm: _armController.text.isNotEmpty ? double.parse(_armController.text) : null,
            leg: _legController.text.isNotEmpty ? double.parse(_legController.text) : null,
          ),
        );
        _waistController.clear();
        _chestController.clear();
        _hipsController.clear();
        _armController.clear();
        _legController.clear();
        _loadMeasurements();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Замер добавлен')),
        );
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Ошибка добавления замера: $e')),
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

  Widget _buildChart(String label, List<double?> values) {
    final spots = <FlSpot>[];
    for (int i = 0; i < values.length; i++) {
      if (values[i] != null) {
        spots.add(FlSpot(i.toDouble(), values[i]!));
      }
    }
    return SizedBox(
      height: 180,
      child: LineChart(
        LineChartData(
          gridData: FlGridData(show: true),
          titlesData: FlTitlesData(
            leftTitles: AxisTitles(
              sideTitles: SideTitles(showTitles: true, reservedSize: 40),
            ),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true,
                getTitlesWidget: (value, meta) {
                  final idx = value.toInt();
                  if (idx >= 0 && idx < _measurements.length) {
                    final date = _measurements[idx].date;
                    return Text('${date.day}/${date.month}', style: const TextStyle(fontSize: 10));
                  }
                  return const Text('');
                },
              ),
            ),
            rightTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
            topTitles: AxisTitles(sideTitles: SideTitles(showTitles: false)),
          ),
          borderData: FlBorderData(show: true),
          lineBarsData: [
            LineChartBarData(
              spots: spots,
              isCurved: true,
              color: Colors.green,
              barWidth: 3,
              dotData: FlDotData(show: true),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Трекер объёмов тела')),
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
                            Row(
                              children: [
                                Expanded(
                                  child: TextFormField(
                                    controller: _waistController,
                                    decoration: const InputDecoration(labelText: 'Талия (см)'),
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: TextFormField(
                                    controller: _chestController,
                                    decoration: const InputDecoration(labelText: 'Грудь (см)'),
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                Expanded(
                                  child: TextFormField(
                                    controller: _hipsController,
                                    decoration: const InputDecoration(labelText: 'Бёдра (см)'),
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: TextFormField(
                                    controller: _armController,
                                    decoration: const InputDecoration(labelText: 'Рука (см)'),
                                    keyboardType: TextInputType.number,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            TextFormField(
                              controller: _legController,
                              decoration: const InputDecoration(labelText: 'Нога (см)'),
                              keyboardType: TextInputType.number,
                            ),
                            const SizedBox(height: 12),
                            ListTile(
                              title: const Text('Дата'),
                              subtitle: Text('${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}'),
                              trailing: const Icon(Icons.calendar_today),
                              onTap: () => _selectDate(context),
                            ),
                            const SizedBox(height: 12),
                            ElevatedButton(
                              onPressed: _addMeasurement,
                              child: const Text('Добавить замер'),
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
                            'История замеров',
                            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 12),
                          _measurements.isEmpty
                              ? const Center(child: Text('Нет замеров'))
                              : ListView.builder(
                                  shrinkWrap: true,
                                  physics: const NeverScrollableScrollPhysics(),
                                  itemCount: _measurements.length,
                                  itemBuilder: (context, i) {
                                    final m = _measurements[i];
                                    return ListTile(
                                      title: Text('${m.date.day}.${m.date.month}.${m.date.year}'),
                                      subtitle: Text(
                                        'Талия: ${m.waist ?? '-'} | Грудь: ${m.chest ?? '-'} | Бёдра: ${m.hips ?? '-'} | Рука: ${m.arm ?? '-'} | Нога: ${m.leg ?? '-'}',
                                      ),
                                      trailing: IconButton(
                                        icon: const Icon(Icons.delete_outline),
                                        onPressed: () async {
                                          await _service.deleteMeasurement(m.id!);
                                          _loadMeasurements();
                                        },
                                      ),
                                    );
                                  },
                                ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  // Графики по каждому параметру
                  if (_measurements.isNotEmpty) ...[
                    const Text('Графики изменений', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 12),
                    _buildChart('Талия', _measurements.map((e) => e.waist).toList()),
                    const SizedBox(height: 12),
                    _buildChart('Грудь', _measurements.map((e) => e.chest).toList()),
                    const SizedBox(height: 12),
                    _buildChart('Бёдра', _measurements.map((e) => e.hips).toList()),
                    const SizedBox(height: 12),
                    _buildChart('Рука', _measurements.map((e) => e.arm).toList()),
                    const SizedBox(height: 12),
                    _buildChart('Нога', _measurements.map((e) => e.leg).toList()),
                  ],
                ],
              ),
            ),
    );
  }

  @override
  void dispose() {
    _waistController.dispose();
    _chestController.dispose();
    _hipsController.dispose();
    _armController.dispose();
    _legController.dispose();
    super.dispose();
  }
} 