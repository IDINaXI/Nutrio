FROM ubuntu:22.04

# Install dependencies
RUN apt-get update && apt-get install -y \
    curl \
    git \
    unzip \
    xz-utils \
    zip \
    libglu1-mesa \
    nginx \
    && rm -rf /var/lib/apt/lists/*

# Install Flutter
RUN git clone https://github.com/flutter/flutter.git -b stable /flutter
ENV PATH="/flutter/bin:${PATH}"

# Verify Flutter installation
RUN flutter doctor

# Set up the app
WORKDIR /app
COPY . .

# Build the app
RUN cd nutrio_app && \
    flutter pub get && \
    flutter build web --release

# Configure nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port
ENV PORT=8080
EXPOSE 8080

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 