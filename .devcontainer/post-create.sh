#!/bin/bash
set -e

echo "Running post-create setup..."

# Install pnpm dependencies for web and valpas-web
echo "Installing frontend dependencies..."
cd /workspace/koski/web && pnpm install
cd /workspace/koski/valpas-web && pnpm install

# Install Playwright browsers (for integration tests)
echo "Installing Playwright browsers..."
cd /workspace/koski/web
npx playwright install chromium

# Make sure command history files exist
touch /commandhistory/.bash_history
touch /commandhistory/.zsh_history
touch /commandhistory/.fish_history

echo "Post-create setup complete!"
echo ""
echo "To use Claude Code, run: claude"
echo "You'll need to authenticate on first use."
