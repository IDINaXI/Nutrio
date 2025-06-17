import 'package:flutter/material.dart';
import '../models/pill_reminder.dart';
import '../services/pill_reminder_service.dart';

class PillReminderScreen extends StatefulWidget {
  final int userId;
  const PillReminderScreen({Key? key, required this.userId}) : super(key: key);

  @override
  State<PillReminderScreen> createState() => _PillReminderScreenState();
}

class _PillReminderScreenState extends State<PillReminderScreen> {
  final PillReminderService _service = PillReminderService();
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _dosageController = TextEditingController();
  final _commentController = TextEditingController();
  TimeOfDay _selectedTime = TimeOfDay.now();
  List<bool> _selectedDays = List.generate(7, (_) => false); // Пн-Вс
  List<PillReminder> _reminders = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _loadReminders();
  }

  Future<void> _loadReminders() async {
    setState(() => _isLoading = true);
    try {
      final data = await _service.getUserReminders(widget.userId);
      setState(() {
        _reminders = data;
        _isLoading = false;
      });
    } catch (e) {
      setState(() => _isLoading = false);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Ошибка загрузки напоминаний: $e')),
      );
    }
  }

  Future<void> _addReminder() async {
    if (_formKey.currentState!.validate()) {
      try {
        final days = <int>[];
        for (int i = 0; i < 7; i++) {
          if (_selectedDays[i]) days.add(i + 1);
        }
        await _service.addReminder(
          PillReminder(
            id: null,
            userId: widget.userId,
            name: _nameController.text,
            dosage: _dosageController.text.isNotEmpty ? _dosageController.text : null,
            comment: _commentController.text.isNotEmpty ? _commentController.text : null,
            time: _selectedTime.hour.toString().padLeft(2, '0') + ':' + _selectedTime.minute.toString().padLeft(2, '0'),
            daysOfWeek: days,
            active: true,
          ),
        );
        _nameController.clear();
        _dosageController.clear();
        _commentController.clear();
        _selectedDays = List.generate(7, (_) => false);
        _selectedTime = TimeOfDay.now();
        _loadReminders();
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Напоминание добавлено')),
        );
      } catch (e) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Ошибка добавления напоминания: $e')),
        );
      }
    }
  }

  Future<void> _pickTime(BuildContext context) async {
    final TimeOfDay? picked = await showTimePicker(
      context: context,
      initialTime: _selectedTime,
    );
    if (picked != null && picked != _selectedTime) {
      setState(() {
        _selectedTime = picked;
      });
    }
  }

  static const List<String> _days = ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Напоминания о таблетках')),
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
                              controller: _nameController,
                              decoration: const InputDecoration(labelText: 'Название таблетки'),
                              validator: (value) => value == null || value.isEmpty ? 'Введите название' : null,
                            ),
                            const SizedBox(height: 12),
                            TextFormField(
                              controller: _dosageController,
                              decoration: const InputDecoration(labelText: 'Дозировка (опционально)'),
                            ),
                            const SizedBox(height: 12),
                            TextFormField(
                              controller: _commentController,
                              decoration: const InputDecoration(labelText: 'Комментарий (опционально)'),
                            ),
                            const SizedBox(height: 12),
                            Row(
                              children: [
                                const Text('Время:'),
                                const SizedBox(width: 12),
                                TextButton(
                                  onPressed: () => _pickTime(context),
                                  child: Text(_selectedTime.format(context)),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),
                            Wrap(
                              spacing: 8,
                              children: List.generate(7, (i) => FilterChip(
                                label: Text(_days[i]),
                                selected: _selectedDays[i],
                                onSelected: (val) {
                                  setState(() {
                                    _selectedDays[i] = val;
                                  });
                                },
                              )),
                            ),
                            const SizedBox(height: 12),
                            ElevatedButton(
                              onPressed: _addReminder,
                              child: const Text('Добавить напоминание'),
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
                            'Список лекарств',
                            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                          ),
                          const SizedBox(height: 12),
                          _reminders.isEmpty
                              ? const Center(child: Text('Нет лекарств'))
                              : ListView.builder(
                                  shrinkWrap: true,
                                  physics: const NeverScrollableScrollPhysics(),
                                  itemCount: _reminders.length,
                                  itemBuilder: (context, i) {
                                    final r = _reminders[i];
                                    return Card(
                                      color: Colors.green[50],
                                      margin: const EdgeInsets.symmetric(vertical: 6),
                                      child: ListTile(
                                        leading: const Icon(Icons.medication, color: Colors.green, size: 32),
                                        title: Text(
                                          r.name,
                                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                                        ),
                                        subtitle: Column(
                                          crossAxisAlignment: CrossAxisAlignment.start,
                                          children: [
                                            Row(
                                              children: [
                                                const Icon(Icons.access_time, size: 16, color: Colors.grey),
                                                const SizedBox(width: 4),
                                                Text(r.time, style: const TextStyle(fontSize: 14)),
                                                const SizedBox(width: 12),
                                                const Icon(Icons.calendar_today, size: 16, color: Colors.grey),
                                                const SizedBox(width: 4),
                                                Text(
                                                  r.daysOfWeek.map((d) => _days[d - 1]).join(", "),
                                                  style: const TextStyle(fontSize: 14),
                                                ),
                                              ],
                                            ),
                                            if (r.dosage != null && r.dosage!.isNotEmpty)
                                              Padding(
                                                padding: const EdgeInsets.only(top: 2),
                                                child: Text('Дозировка: ${r.dosage}', style: const TextStyle(fontSize: 13)),
                                              ),
                                            if (r.comment != null && r.comment!.isNotEmpty)
                                              Padding(
                                                padding: const EdgeInsets.only(top: 2),
                                                child: Text(r.comment!, style: const TextStyle(fontSize: 13, color: Colors.grey)),
                                              ),
                                          ],
                                        ),
                                        trailing: IconButton(
                                          icon: const Icon(Icons.delete_outline, color: Colors.red),
                                          onPressed: () async {
                                            await _service.deleteReminder(r.id!);
                                            _loadReminders();
                                          },
                                        ),
                                      ),
                                    );
                                  },
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
    _nameController.dispose();
    _dosageController.dispose();
    _commentController.dispose();
    super.dispose();
  }
} 