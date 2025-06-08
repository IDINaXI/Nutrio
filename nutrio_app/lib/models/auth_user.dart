class AuthUser {
  final int id;
  final String name;
  final String email;
  final int age;
  final double height;
  final double weight;
  final String gender;
  final String goal;
  final String activityLevel;
  final List<String> allergies;

  AuthUser({
    required this.id,
    required this.name,
    required this.email,
    required this.age,
    required this.height,
    required this.weight,
    required this.gender,
    required this.goal,
    required this.activityLevel,
    required this.allergies,
  });

  factory AuthUser.fromJson(Map<String, dynamic> json) {
    return AuthUser(
      id: json['id'] as int,
      name: json['name'] as String,
      email: json['email'] as String,
      age: json['age'] as int,
      height: (json['height'] as num).toDouble(),
      weight: (json['weight'] as num).toDouble(),
      gender: json['gender'] as String,
      goal: json['goal'] as String,
      activityLevel: json['activityLevel'] as String,
      allergies: List<String>.from(json['allergies'] as List),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'age': age,
      'height': height,
      'weight': weight,
      'gender': gender,
      'goal': goal,
      'activityLevel': activityLevel,
      'allergies': allergies,
    };
  }
} 