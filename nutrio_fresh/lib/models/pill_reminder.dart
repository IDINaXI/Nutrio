class PillReminder {
  final int? id;
  final int userId;
  final String name;
  final String? dosage;
  final String? comment;
  final String time; // формат HH:mm
  final List<int> daysOfWeek; // 1=Пн, 7=Вс
  final bool active;

  PillReminder({
    this.id,
    required this.userId,
    required this.name,
    this.dosage,
    this.comment,
    required this.time,
    required this.daysOfWeek,
    this.active = true,
  });

  factory PillReminder.fromJson(Map<String, dynamic> json) => PillReminder(
        id: json['id'],
        userId: json['userId'],
        name: json['name'],
        dosage: json['dosage'],
        comment: json['comment'],
        time: json['time'],
        daysOfWeek: List<int>.from(json['daysOfWeek']),
        active: json['active'] ?? true,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'userId': userId,
        'name': name,
        'dosage': dosage,
        'comment': comment,
        'time': time,
        'daysOfWeek': daysOfWeek,
        'active': active,
      };
} 