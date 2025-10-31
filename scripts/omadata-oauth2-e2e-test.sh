#!/bin/bash
set -eou pipefail
echo "Running OmaDataOAuth2 Sample Playwright tests, shard $2 of $3, using backend on port $1"
cd $(dirname "$0")/../omadata-oauth2-sample/client

KOSKI_BACKEND_PORT="$1" pnpm run playwright:test --shard=$2/$3
