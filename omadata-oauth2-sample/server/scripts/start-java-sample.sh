#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
backend_host="${KOSKI_BACKEND_HOST:-http://$(node "$ROOT_DIR/scripts/getmyip.js"):7021}"

cd "$ROOT_DIR/../java"
KOSKI_BACKEND_HOST="$backend_host" ./mvnw spring-boot:run
