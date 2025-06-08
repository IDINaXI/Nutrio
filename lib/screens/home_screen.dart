Card(
  elevation: 3,
  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
  child: Padding(
    padding: EdgeInsets.symmetric(
      horizontal: isSmallScreen ? 14 : 18,
      vertical: isSmallScreen ? 16 : 20,
    ),
    child: Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
                  Text(
                    'Быстрые действия',
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
        SizedBox(height: isSmallScreen ? 12 : 16),
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
            SizedBox(width: isSmallScreen ? 8 : 12),
                      Expanded(
                        child: _QuickActionCard(
                          icon: Icons.trending_up,
                          title: 'Прогресс',
                          onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => WeightTrackingScreen(userId: _user!.id),
                      ),
                  );
                },
              ),
            ),
          ],
        ),
        SizedBox(height: isSmallScreen ? 8 : 12),
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
                      builder: (context) => BodyMeasurementScreen(userId: _user!.id),
            ),
                  );
                },
              ),
            ),
            SizedBox(width: isSmallScreen ? 8 : 12),
            Expanded(
              child: _QuickActionCard(
                icon: Icons.medication,
                title: 'Таблетки',
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => PillReminderScreen(userId: _user!.id),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
        SizedBox(height: isSmallScreen ? 8 : 12),
        Row(
          children: [
            Expanded(
              child: _QuickActionCard(
                icon: Icons.description,
                title: 'Отчёт для врача',
                onTap: () {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => ReportScreen(userId: _user!.id),
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