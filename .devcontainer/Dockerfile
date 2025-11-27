FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Install basic dependencies
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends \
    build-essential \
    curl \
    wget \
    ca-certificates \
    gnupg \
    lsb-release \
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

# Install pnpm globally
RUN npm install -g pnpm

# Create directory for Playwright browsers
RUN mkdir -p /ms-playwright && chmod 777 /ms-playwright

# Install Claude Code CLI
RUN curl -fsSL https://static.anthropic.com/cli/install.sh | sh

# Setup command history persistence
RUN SNIPPET="export PROMPT_COMMAND='history -a' && export HISTFILE=/commandhistory/.bash_history" \
    && mkdir -p /commandhistory \
    && touch /commandhistory/.bash_history \
    && chown -R developer /commandhistory \
    && echo "$SNIPPET" >> "/home/developer/.bashrc"

# Setup zsh history
RUN echo "export HISTFILE=/commandhistory/.zsh_history" >> "/home/developer/.zshrc" \
    && echo "export HISTSIZE=10000" >> "/home/developer/.zshrc" \
    && echo "export SAVEHIST=10000" >> "/home/developer/.zshrc" \
    && echo "setopt SHARE_HISTORY" >> "/home/developer/.zshrc"

# Setup fish history
RUN mkdir -p /home/developer/.config/fish \
    && echo "set -gx fish_history_path /commandhistory/.fish_history" >> "/home/developer/.config/fish/config.fish" \
    && chown -R developer:developer /home/developer/.config

# Install Xvfb startup script for headless display
RUN echo '#!/bin/bash\nXvfb :99 -screen 0 1920x1080x24 > /dev/null 2>&1 &' > /usr/local/bin/start-xvfb.sh \
    && chmod +x /usr/local/bin/start-xvfb.sh

# Start Xvfb by default
RUN echo '/usr/local/bin/start-xvfb.sh' >> /home/developer/.bashrc \
    && echo '/usr/local/bin/start-xvfb.sh' >> /home/developer/.zshrc

USER developer
