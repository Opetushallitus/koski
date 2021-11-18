#!/bin/bash
set -eou pipefail
echo "Running Jest test chunk $2 of $3, using backend on $1"
cd $(dirname "$0")/../valpas-web
npm ci
BACKEND_HOST="$1" TEST_CHUNK="$2/$3" npm run test:integration
