FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Set UTF-8 locale to support Finnish characters (å, ä, ö)
ENV LANG=en_US.UTF-8 \
    LANGUAGE=en_US:en \
    LC_ALL=en_US.UTF-8

# Install basic dependencies and tools
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    build-essential \
    curl \
    wget \
    ca-certificates \
    gnupg \
    lsb-release \
    locales \
    fish \
    xvfb \
    # Dependencies for Chromium/Playwright
    libnss3 \
    libnspr4 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libdrm2 \
    libdbus-1-3 \
    libxkbcommon0 \
    libxcomposite1 \
    libxdamage1 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libasound2 \
    libpango-1.0-0 \
    libcairo2 \
    libatspi2.0-0 \
    libxshmfence1 \
    # Additional browser dependencies
    fonts-liberation \
    libappindicator3-1 \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Generate UTF-8 locale
RUN locale-gen en_US.UTF-8 && update-locale LANG=en_US.UTF-8

# Create directory for Playwright browsers
RUN mkdir -p /ms-playwright && chmod 777 /ms-playwright

# Create directory for command history
RUN mkdir -p /commandhistory && chmod 777 /commandhistory

# Install Claude Code CLI
RUN curl -fsSL https://static.anthropic.com/cli/install.sh | sh

# Install Xvfb startup script for headless display
RUN echo '#!/bin/bash\nXvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &' > /usr/local/bin/start-xvfb.sh \
    && chmod +x /usr/local/bin/start-xvfb.sh
