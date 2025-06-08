document.addEventListener('DOMContentLoaded', function() {
    const mealPlan = JSON.parse(localStorage.getItem('mealPlan'));
    if (!mealPlan) {
        window.location.href = '/';
        return;
    }

    // Отображаем общую информацию
    document.getElementById('totalCalories').textContent = mealPlan.totalCalories;
    document.getElementById('totalProtein').textContent = mealPlan.totalProtein;
    document.getElementById('totalCarbs').textContent = mealPlan.totalCarbs;
    document.getElementById('totalFat').textContent = mealPlan.totalFat;

    // Отображаем план питания по дням
    const mealPlanContainer = document.getElementById('mealPlanContainer');
    mealPlan.days.forEach(day => {
        const dayCard = document.createElement('div');
        dayCard.className = 'card mb-3';
        dayCard.innerHTML = `
            <div class="card-header">
                <h5 class="mb-0">
                    <button class="btn btn-link" type="button" data-bs-toggle="collapse" 
                            data-bs-target="#day${day.dayOfWeek}">
                        ${day.dayOfWeek}
                    </button>
                </h5>
            </div>
            <div id="day${day.dayOfWeek}" class="collapse" data-bs-parent="#mealPlanContainer">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-4">
                            <h6>Завтрак</h6>
                            <p>${day.breakfast.name}</p>
                            <p>Калории: ${day.breakfast.calories}</p>
                            <p>Белки: ${day.breakfast.protein}g</p>
                            <p>Углеводы: ${day.breakfast.carbs}g</p>
                            <p>Жиры: ${day.breakfast.fat}g</p>
                        </div>
                        <div class="col-md-4">
                            <h6>Обед</h6>
                            <p>${day.lunch.name}</p>
                            <p>Калории: ${day.lunch.calories}</p>
                            <p>Белки: ${day.lunch.protein}g</p>
                            <p>Углеводы: ${day.lunch.carbs}g</p>
                            <p>Жиры: ${day.lunch.fat}g</p>
                        </div>
                        <div class="col-md-4">
                            <h6>Ужин</h6>
                            <p>${day.dinner.name}</p>
                            <p>Калории: ${day.dinner.calories}</p>
                            <p>Белки: ${day.dinner.protein}g</p>
                            <p>Углеводы: ${day.dinner.carbs}g</p>
                            <p>Жиры: ${day.dinner.fat}g</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        mealPlanContainer.appendChild(dayCard);
    });

    // Инициализируем Bootstrap collapse
    const collapseElements = document.querySelectorAll('.collapse');
    collapseElements.forEach(collapse => {
        new bootstrap.Collapse(collapse, {
            toggle: false
        });
    });
}); 