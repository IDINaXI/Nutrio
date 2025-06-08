document.getElementById('profileForm').addEventListener('submit', async function(e) {
    e.preventDefault();

    const userData = {
        name: document.getElementById('name').value,
        age: parseInt(document.getElementById('age').value),
        height: parseFloat(document.getElementById('height').value),
        weight: parseFloat(document.getElementById('weight').value),
        gender: document.getElementById('gender').value,
        goal: document.getElementById('goal').value,
        allergies: document.getElementById('allergies').value
            .split(',')
            .map(item => item.trim())
            .filter(item => item !== '')
    };

    try {
        const response = await fetch('/api/meal-plans/generate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            const mealPlan = await response.json();
            // Сохраняем план питания в localStorage
            localStorage.setItem('mealPlan', JSON.stringify(mealPlan));
            // Перенаправляем на страницу с планом питания
            window.location.href = '/meal-plan';
        } else {
            alert('Ошибка при создании плана питания. Пожалуйста, попробуйте снова.');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Произошла ошибка. Пожалуйста, попробуйте снова.');
    }
}); 