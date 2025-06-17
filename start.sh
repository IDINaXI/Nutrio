#!/bin/bash

# Остановка предыдущих процессов
echo "Stopping previous processes..."
pkill -f "java -jar"
pkill -f "nginx"

# Запуск Spring Boot приложения
echo "Starting Spring Boot application..."
./mvnw spring-boot:run &

# Запуск Nginx
echo "Starting Nginx..."
nginx -c "$(pwd)/nginx.conf"

echo "Application is running!"
echo "Access it at: http://localhost:80" 