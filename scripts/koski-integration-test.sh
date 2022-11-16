#!/bin/bash
set -eou pipefail
echo "Running Koski Playwright tests, using backend on $1"
cd $(dirname "$0")/../web

BACKEND_HOST="$1" npm run playwright:test
