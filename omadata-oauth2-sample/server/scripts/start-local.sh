#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

backend_host="${KOSKI_BACKEND_HOST:-http://$(node "$ROOT_DIR/scripts/getmyip.js"):7021}"
resource_endpoint_url="${RESOURCE_ENDPOINT_URL:-https://localhost:7022/koski/api/omadata-oauth2/resource-server}"
client_suffix="${NOLOGOUT:-}"
client_id="${CLIENT_ID:-omadataoauth2sample${client_suffix}}"

export KOSKI_BACKEND_HOST="$backend_host"
export RESOURCE_ENDPOINT_URL="$resource_endpoint_url"
export CLIENT_ID="$client_id"
export ENABLE_LOCAL_MTLS="${ENABLE_LOCAL_MTLS:-true}"
export NODE_EXTRA_CA_CERTS="$ROOT_DIR/testca/certs/root-ca.crt"

cd "$ROOT_DIR"
tsx watch --clear-screen=false src/index.ts
