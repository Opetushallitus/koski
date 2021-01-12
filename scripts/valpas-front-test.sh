#!/bin/bash
set -eou pipefail
echo "Running Jest tests, using backend on $1"
cd $(dirname "$0")/../valpas-web
npm ci
BACKEND_URL="$1" npm run test:integration
