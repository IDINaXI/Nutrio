import 'package:flutter/material.dart';
import '../models/auth_user.dart';
import '../models/meal_plan.dart';
import '../services/auth_service.dart';
import '../services/api_service.dart';
import '../services/ai_service.dart';
import 'profile_screen.dart';
import 'login_screen.dart';
import 'meal_plan_screen.dart';
import 'weight_tracking_screen.dart';
import 'body_measurement_screen.dart';
import 'pill_reminder_screen.dart';
import 'report_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _authService = AuthService();
  final _apiService = ApiService();
  final _aiService = AIService();
  AuthUser? _user;
  MealPlan? _mealPlan;
  bool _isLoading = true;
  bool _isGenerating = false;

  @override
  void initState() {
    super.initState();
    _checkAuth();
    _aiService.setToken(_authService.token!);
  }

  Future<void> _checkAuth() async {
    if (_authService.token == null) {
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => const LoginScreen(),
          ),
        );
      }
    } else {
      _loadUserData();
    }
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    if (!_isLoading && _user == null) {
      _loadUserData();
    }
  }

  Future<void> _loadUserData() async {
    try {
      if (_authService.token == null) {
        throw Exception('Токен не найден');
      }
      final user = await _authService.getUserData(_authService.token!);
      print('Loaded user data: ${user.name}, age=${user.age}, weight=${user.weight}, height=${user.height}, activityLevel=${user.activityLevel}, goal=${user.goal}');
      
      if (mounted) {
        setState(() {
          _user = user;
          _isLoading = false;
        });
      }
    } catch (e) {
      print('Error loading user data: $e');
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        WidgetsBinding.instance.addPostFrameCallback((_) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(e.toString()),
              backgroundColor: Colors.red,
              duration: const Duration(seconds: 5),
            ),
          );
        });
      }
    }
  }

  Future<void> _generateMealPlan() async {
    if (_user == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Пожалуйста, подождите загрузки данных пользователя'),
          backgroundColor: Colors.red,
        ),
      );
      return;
    }

    setState(() {
      _isGenerating = true;
    });

    try {
      print('Generating meal plan with user data: \\${_user!.name}, age=\\${_user!.age}, weight=\\${_user!.weight}, height=\\${_user!.height}, activityLevel=\\${_user!.activityLevel}, goal=\\${_user!.goal}');
      await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => MealPlanScreen(user: _user!),
        ),
      );
      // После возврата с экрана генерации — обновить план питания
      await _loadMealPlan();
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Ошибка создания плана питания: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isGenerating = false;
        });
      }
    }
  }

  Future<void> _logout() async {
    try {
      await _authService.logout();
      if (mounted) {
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => const LoginScreen(),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Ошибка при выходе: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _loadMealPlan() async {
    if (_user == null) {
      print('Cannot load meal plan: user is null');
      return;
    }

    print('Loading meal plan for user ID: ${_user!.id}');
    print('Auth token: ${_authService.token}');
    
    try {
      final plan = await _apiService.fetchSavedMealPlan(_user!.id);
      print('Loaded meal plan: ${plan?.toJson()}');
      
      if (mounted) {
        setState(() {
          _mealPlan = plan;
          _isLoading = false;
        });
      }
    } catch (e) {
      print('Error loading meal plan: $e');
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Ошибка загрузки плана питания: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  // Добавляю метод для получения плана на сегодня
  WeekDayPlan? _getTodayPlan() {
    if (_mealPlan == null || _mealPlan!.week.isEmpty) {
      return null;
    }
    final now = DateTime.now();
    final today = now.weekday - 1; // Monday = 0
    if (today < _mealPlan!.week.length) {
      return _mealPlan!.week[today];
    }
    return null;
  }

  @override
  Widget build(BuildContext context) {
    if (_user == null) {
      return const Scaffold(
        body: Center(
          child: Text('Пользователь не найден'),
        ),
      );
    }

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            colors: [Color(0xFFF8FBF5), Color(0xFFE8F5E9)],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: SafeArea(
          child: CustomScrollView(
            slivers: [
              // App Bar
              SliverAppBar(
                floating: true,
                snap: true,
                backgroundColor: Colors.transparent,
                elevation: 0,
                title: Row(
                  children: [
                    Icon(Icons.eco, color: Theme.of(context).colorScheme.primary),
                    const SizedBox(width: 8),
                    const Text('Nutrio', style: TextStyle(color: Colors.red)),
                  ],
                ),
                actions: [
                  IconButton(
                    icon: const Icon(Icons.person_outline),
                    onPressed: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (context) => ProfileScreen(user: _user!),
                        ),
                      );
                    },
                  ),
                ],
              ),
              // Main Content
              SliverToBoxAdapter(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // User Info Card
                    // Welcome Section
                    Card(
                      elevation: 4,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
                        child: Row(
                          children: [
                            CircleAvatar(
                              radius: 28,
                              backgroundColor: Theme.of(context).colorScheme.primary.withOpacity(0.1),
                              child: Icon(Icons.person, size: 36, color: Theme.of(context).colorScheme.primary),
                            ),
                            const SizedBox(width: 20),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    'С возвращением,',
                                    style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                                      color: Colors.grey[700],
                                    ),
                                  ),
                                  const SizedBox(height: 2),
                                  Text(
                                    _user!.name,
                                    style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                                      fontWeight: FontWeight.bold,
                                      color: Theme.of(context).colorScheme.primary,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 28),
                    // Quick Actions Section
                    Card(
                      elevation: 3,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 20),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'Быстрые действия',
                              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 16),
                            Row(
                              children: [
                                Expanded(
                                  child: _QuickActionCard(
                                    icon: Icons.restaurant_menu,
                                    title: 'План питания',
                                    onTap: () {
                                      Navigator.push(
                                        context,
                                        MaterialPageRoute(
                                          builder: (context) => MealPlanScreen(user: _user!),
                                        ),
                                      );
                                    },
                                  ),
                                ),
                                const SizedBox(width: 16),
                                Expanded(
                                  child: _QuickActionCard(
                                    icon: Icons.trending_up,
                                    title: 'Прогресс',
                                    onTap: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) => WeightTrackingScreen(
                                              userId: _user!.id,
                                            ),
                                          ),
                                        );
                                    },
                                  ),
                                ),
                              ],
                            ),
                              const SizedBox(height: 12),
                              Row(
                                children: [
                                  Expanded(
                                    child: _QuickActionCard(
                                      icon: Icons.straighten,
                                      title: 'Объёмы тела',
                                      onTap: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) => BodyMeasurementScreen(
                                              userId: _user!.id,
                                            ),
                                          ),
                                        );
                                      },
                                    ),
                                  ),
                                  const SizedBox(width: 16),
                                  Expanded(
                                    child: _QuickActionCard(
                                      icon: Icons.medication,
                                      title: 'Таблетки',
                                      onTap: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) => PillReminderScreen(
                                              userId: _user!.id,
                                            ),
                                          ),
                                        );
                                      },
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 12),
                              Row(
                                children: [
                                  Expanded(
                                    child: _QuickActionCard(
                                      icon: Icons.picture_as_pdf,
                                      title: 'Отчёт для врача',
                                      onTap: () {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) => ReportScreen(
                                              userId: _user!.id,
                                            ),
                                          ),
                                        );
                                      },
                                      isWide: true,
                                    ),
                                  ),
                                ],
                              ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 28),
                    // Today Plan Section
                    Card(
                      elevation: 3,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 24),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              'План на сегодня',
                              style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                            const SizedBox(height: 16),
                            if (_isLoading)
                              const Center(child: CircularProgressIndicator())
                            else
                              (() {
                                final todayPlan = _getTodayPlan();
                                if (todayPlan == null) {
                                  return _NoPlanCard(onGenerate: _generateMealPlan, buttonText: 'Показать план на сегодня');
                                }
                                return Column(
                                  children: [
                                    _ActivityCard(
                                      icon: Icons.breakfast_dining,
                                      title: 'Завтрак',
                                      subtitle: todayPlan.breakfast.name,
                                      calories: todayPlan.breakfast.calories.toInt(),
                                    ),
                                    const SizedBox(height: 12),
                                    _ActivityCard(
                                      icon: Icons.lunch_dining,
                                      title: 'Обед',
                                      subtitle: todayPlan.lunch.name,
                                      calories: todayPlan.lunch.calories.toInt(),
                                    ),
                                    const SizedBox(height: 12),
                                    _ActivityCard(
                                      icon: Icons.dinner_dining,
                                      title: 'Ужин',
                                      subtitle: todayPlan.dinner.name,
                                      calories: todayPlan.dinner.calories.toInt(),
                                    ),
                                    const SizedBox(height: 12),
                                    _ActivityCard(
                                      icon: Icons.emoji_food_beverage,
                                      title: 'Перекус',
                                      subtitle: todayPlan.snack.name,
                                      calories: todayPlan.snack.calories.toInt(),
                                    ),
                                  ],
                                );
                              })(),
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
      ),
    );
  }
}

class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final VoidCallback onTap;
  final bool isWide;

  const _QuickActionCard({
    required this.icon,
    required this.title,
    required this.onTap,
    this.isWide = false,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: EdgeInsets.symmetric(
            vertical: isWide ? 20 : 16,
            horizontal: isWide ? 24 : 16,
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(
                icon,
                size: isWide ? 36 : 32,
                color: Theme.of(context).colorScheme.primary,
              ),
              SizedBox(width: isWide ? 16 : 8),
              Flexible(
                child: Text(
                title,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                  textAlign: TextAlign.center,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _ActivityCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final int calories;

  const _ActivityCard({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.calories,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        leading: Container(
          padding: const EdgeInsets.all(8),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.primary.withOpacity(0.1),
            borderRadius: BorderRadius.circular(8),
          ),
          child: Icon(
            icon,
            color: Theme.of(context).colorScheme.primary,
          ),
        ),
        title: Text(title),
        subtitle: Text(subtitle),
        trailing: Text(
          '$calories ккал',
          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
            color: Theme.of(context).colorScheme.primary,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}

// В конец файла добавляю виджет _NoPlanCard
class _NoPlanCard extends StatelessWidget {
  final VoidCallback onGenerate;
  final String buttonText;

  const _NoPlanCard({required this.onGenerate, this.buttonText = 'Создать план питания'});

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 6,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          gradient: LinearGradient(
            colors: [
              Theme.of(context).colorScheme.primary.withOpacity(0.08),
              Theme.of(context).colorScheme.secondary.withOpacity(0.06),
            ],
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
          ),
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 36.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  boxShadow: [
                    BoxShadow(
                      color: Theme.of(context).colorScheme.primary.withOpacity(0.18),
                      blurRadius: 24,
                      offset: const Offset(0, 8),
                    ),
                  ],
                  gradient: LinearGradient(
                    colors: [
                      Theme.of(context).colorScheme.primary.withOpacity(0.18),
                      Theme.of(context).colorScheme.primary.withOpacity(0.08),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
                padding: const EdgeInsets.all(24),
                child: Icon(
                  Icons.restaurant_menu,
                  size: 56,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                'У вас еще нет плана питания',
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: Theme.of(context).colorScheme.primary,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 12),
              Text(
                'Создайте персональный план питания на основе ваших целей и предпочтений',
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: Colors.grey[700],
                  fontSize: 16,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Theme.of(context).colorScheme.primary,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 18),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                    elevation: 2,
                  ),
                  onPressed: onGenerate,
                  child: Text(
                    buttonText,
                    style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w600),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
} 