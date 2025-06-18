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

# Navigate to the web build directory
cd nutrio_app/build/web

# Install serve if not already installed
if ! command -v serve &> /dev/null; then
    echo "Installing serve..."
    npm install -g serve
fi

# Start the server with proper error handling
echo "Starting server..."
serve -s . --listen 3000 --no-clipboard --no-request-logging

echo "Application is running!"
echo "Access it at: http://localhost:80" 