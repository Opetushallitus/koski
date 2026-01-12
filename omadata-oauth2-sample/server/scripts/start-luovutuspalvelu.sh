#!/usr/bin/env bash
set -euo pipefail

COMMAND="${1:-start}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

backend_host="${KOSKI_BACKEND_HOST:-http://$(node "$ROOT_DIR/scripts/getmyip.js"):7021}"
client_suffix="${NOLOGOUT:-}"
client_user="${CLIENT_USERNAME:-omadataoauth2sample${client_suffix}}"
client_pass="${CLIENT_PASSWORD:-omadataoauth2sample${client_suffix}}"

export KOSKI_BACKEND_HOST="$backend_host"
export CLIENT_USERNAME="$client_user"
export CLIENT_PASSWORD="$client_pass"

pnpm run --prefix "$ROOT_DIR/../../koski-luovutuspalvelu/proxy" "$COMMAND"
