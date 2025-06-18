FROM ubuntu:22.04

# Prevent interactive prompts during package installation
ENV DEBIAN_FRONTEND=noninteractive

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    xz-utils \
    zip \
    libglu1-mesa \
    nginx \
    clang \
    cmake \
    ninja-build \
    pkg-config \
    libgtk-3-dev \
    liblzma-dev \
    && rm -rf /var/lib/apt/lists/*

# Install Flutter
RUN git clone https://github.com/flutter/flutter.git -b stable /flutter
ENV PATH="/flutter/bin:${PATH}"

# Verify Flutter installation and accept licenses
RUN flutter doctor -v && \
    flutter config --enable-web && \
    yes | flutter doctor --android-licenses

# Set up the app
WORKDIR /app
COPY . .

# Build the app with verbose output
RUN cd nutrio_app && \
    flutter pub get --verbose && \
    flutter build web --release --verbose

# Configure nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
ENV PORT=8080
EXPOSE 8080

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 