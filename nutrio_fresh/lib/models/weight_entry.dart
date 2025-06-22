class WeightEntry {
  final DateTime date;
  final double weight;

  WeightEntry({required this.date, required this.weight});

  factory WeightEntry.fromJson(Map<String, dynamic> json) => WeightEntry(
    date: DateTime.parse(json['date']),
    weight: (json['weight'] as num).toDouble(),
  );

  Map<String, dynamic> toJson() => {
    'date': date.toIso8601String(),
    'weight': weight,
  };
} 