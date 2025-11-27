#!/bin/bash
set -e

echo "Running post-create setup..."

# Fix ownership of mounted volumes (they may be created as root)
echo "Fixing volume permissions..."
sudo chown -R vscode:vscode /home/vscode/.m2 2>/dev/null || true
sudo chown -R vscode:vscode /home/vscode/.config 2>/dev/null || true
sudo chown -R vscode:vscode /home/vscode/.local 2>/dev/null || true

# Setup shell history persistence
echo "Setting up shell history..."
mkdir -p /commandhistory
touch /commandhistory/.bash_history
touch /commandhistory/.zsh_history
touch /commandhistory/.fish_history

# Setup bash history
BASH_SNIPPET="export PROMPT_COMMAND='history -a' && export HISTFILE=/commandhistory/.bash_history"
if ! grep -q "HISTFILE=/commandhistory" ~/.bashrc; then
    echo "$BASH_SNIPPET" >> ~/.bashrc
fi

# Setup zsh history
if ! grep -q "HISTFILE=/commandhistory" ~/.zshrc 2>/dev/null; then
    cat >> ~/.zshrc << 'EOF'
export HISTFILE=/commandhistory/.zsh_history
export HISTSIZE=10000
export SAVEHIST=10000
setopt SHARE_HISTORY
EOF
fi

# Setup fish history
mkdir -p ~/.config/fish
if [ ! -f ~/.config/fish/config.fish ] || ! grep -q "fish_history_path" ~/.config/fish/config.fish; then
    echo "set -gx fish_history_path /commandhistory/.fish_history" >> ~/.config/fish/config.fish
fi

# Setup Xvfb to start automatically
if ! grep -q "start-xvfb.sh" ~/.bashrc; then
    echo '/usr/local/bin/start-xvfb.sh' >> ~/.bashrc
fi
if [ -f ~/.zshrc ] && ! grep -q "start-xvfb.sh" ~/.zshrc; then
    echo '/usr/local/bin/start-xvfb.sh' >> ~/.zshrc
fi

# Install pnpm globally (Node.js is available now via features)
echo "Installing pnpm..."
npm install -g pnpm

# Install pnpm dependencies for web and valpas-web
echo "Installing frontend dependencies..."
cd /workspace/koski/web && pnpm install
cd /workspace/koski/valpas-web && pnpm install

# Install Playwright browsers (for integration tests)
echo "Installing Playwright browsers..."
cd /workspace/koski/web
npx playwright install chromium

echo ""
echo "Post-create setup complete!"
echo ""
echo "Available shells: bash (default), zsh, fish"
echo "To use Claude Code, run: claude"
echo "You'll need to authenticate on first use."
