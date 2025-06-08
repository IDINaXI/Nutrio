class BodyMeasurement {
  final int? id;
  final int userId;
  final DateTime date;
  final double? waist;
  final double? chest;
  final double? hips;
  final double? arm;
  final double? leg;

  BodyMeasurement({
    this.id,
    required this.userId,
    required this.date,
    this.waist,
    this.chest,
    this.hips,
    this.arm,
    this.leg,
  });

  factory BodyMeasurement.fromJson(Map<String, dynamic> json) => BodyMeasurement(
        id: json['id'],
        userId: json['userId'],
        date: DateTime.parse(json['date']),
        waist: (json['waist'] as num?)?.toDouble(),
        chest: (json['chest'] as num?)?.toDouble(),
        hips: (json['hips'] as num?)?.toDouble(),
        arm: (json['arm'] as num?)?.toDouble(),
        leg: (json['leg'] as num?)?.toDouble(),
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'userId': userId,
        'date': date.toIso8601String(),
        'waist': waist,
        'chest': chest,
        'hips': hips,
        'arm': arm,
        'leg': leg,
      };
} 