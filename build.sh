#!/bin/bash

# Install Flutter dependencies
cd nutrio_app
flutter pub get

# Build Flutter web app
flutter build web --release

# Install serve globally
npm install -g serve

# Move to the build directory
cd build/web

# Start the server
serve -s . 