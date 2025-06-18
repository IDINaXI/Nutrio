import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:intl/intl.dart';
import 'dart:html';
import '../services/auth_service.dart';
import 'custom_date_picker.dart';

class ReportScreen extends StatefulWidget {
  final int userId;
  const ReportScreen({Key? key, required this.userId}) : super(key: key);

  @override
  State<ReportScreen> createState() => _ReportScreenState();
}

class _ReportScreenState extends State<ReportScreen> {
  DateTime _from = DateTime.now().subtract(const Duration(days: 30));
  DateTime _to = DateTime.now();
  bool _isLoading = false;
  String? _status;

  Future<void> _pickDate(BuildContext context, bool isFrom) async {
    final DateTime? picked = await showCustomDatePicker(
      context: context,
      initialDate: isFrom ? _from : _to,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );
    if (picked != null) {
      setState(() {
        if (isFrom) {
          _from = picked;
        } else {
          _to = picked;
        }
      });
    }
  }

  Future<void> _downloadReport() async {
    setState(() {
      _isLoading = true;
      _status = null;
    });
    try {
      final token = AuthService().token;
      final url = Uri.parse('https://nutrio-production.up.railway.app/api/reports/user/${widget.userId}?from=${DateFormat('yyyy-MM-dd').format(_from)}&to=${DateFormat('yyyy-MM-dd').format(_to)}');
      final response = await http.get(url, headers: {
        'Authorization': 'Bearer $token',
      });
      if (response.statusCode == 200) {
        // Для Flutter Web: открыть PDF в новой вкладке
        // Для мобильных: сохранить файл (требуется плагин)
        final bytes = response.bodyBytes;
        final blob = Blob([bytes]);
        final url2 = Url.createObjectUrlFromBlob(blob);
        AnchorElement(href: url2)
          ..setAttribute('download', 'nutrio_report.pdf')
          ..click();
        Url.revokeObjectUrl(url2);
        setState(() => _status = 'Отчёт успешно скачан!');
      } else {
        setState(() => _status = 'Ошибка: ${response.statusCode}');
      }
    } catch (e) {
      setState(() => _status = 'Ошибка: $e');
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Отчёт для врача')),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text('Выберите период для отчёта:', style: TextStyle(fontSize: 16)),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: InkWell(
                    onTap: () => _pickDate(context, true),
                    child: InputDecorator(
                      decoration: const InputDecoration(labelText: 'С'),
                      child: Text(DateFormat('dd.MM.yyyy').format(_from)),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: InkWell(
                    onTap: () => _pickDate(context, false),
                    child: InputDecorator(
                      decoration: const InputDecoration(labelText: 'По'),
                      child: Text(DateFormat('dd.MM.yyyy').format(_to)),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            ElevatedButton.icon(
              onPressed: _isLoading ? null : _downloadReport,
              icon: const Icon(Icons.picture_as_pdf),
              label: const Text('Скачать PDF-отчёт'),
              style: ElevatedButton.styleFrom(padding: const EdgeInsets.symmetric(vertical: 16)),
            ),
            if (_isLoading) ...[
              const SizedBox(height: 24),
              const Center(child: CircularProgressIndicator()),
            ],
            if (_status != null) ...[
              const SizedBox(height: 24),
              Center(child: Text(_status!, style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold))),
            ],
          ],
        ),
      ),
    );
  }
} 