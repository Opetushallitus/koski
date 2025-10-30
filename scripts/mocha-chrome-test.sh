#!/bin/bash
set -eou pipefail
echo "Running mocha tests against $1"
PORT=$((12000 + RANDOM % 1000))
USER_DATA_DIR="/tmp/lighthouse-$PORT"
(cd $(dirname "$0")/../web && MOCHA_URL="$1" pnpm run test -- --port=$PORT --user-data-dir="$USER_DATA_DIR")
