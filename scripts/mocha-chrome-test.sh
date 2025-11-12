#!/bin/bash
set -eou pipefail
MOCHA_URL="$1"
echo "Running mocha tests against $MOCHA_URL"
PORT=$((12000 + RANDOM % 1000))
USER_DATA_DIR="/tmp/lighthouse-$PORT"
export MOCHA_URL
(cd $(dirname "$0")/../web && pnpm run test --port=$PORT --user-data-dir="$USER_DATA_DIR")
