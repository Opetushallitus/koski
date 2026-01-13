#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

backend_host="${KOSKI_BACKEND_HOST:-http://$(node "$ROOT_DIR/scripts/getmyip.js"):7021}"

export KOSKI_BACKEND="$backend_host"
export PROXY_PORT="${PROXY_PORT:-7022}"

echo "Starting ALB proxy on port $PROXY_PORT -> $KOSKI_BACKEND"
node "$ROOT_DIR/scripts/alb-proxy.mjs"
