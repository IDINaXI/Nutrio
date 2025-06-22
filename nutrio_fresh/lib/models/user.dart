 class User {
  final String name;
  final int age;
  final double height;
  final double weight;
  final String gender;
  final String goal;
  final List<String> allergies;

  User({
    required this.name,
    required this.age,
    required this.height,
    required this.weight,
    required this.gender,
    required this.goal,
    required this.allergies,
  });

  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'age': age,
      'height': height,
      'weight': weight,
      'gender': gender,
      'goal': goal,
      'allergies': allergies,
    };
  }

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      name: json['name'],
      age: json['age'],
      height: json['height'],
      weight: json['weight'],
      gender: json['gender'],
      goal: json['goal'],
      allergies: List<String>.from(json['allergies']),
    );
  }
}