#!/bin/bash
set -eou pipefail
echo "Running mocha tests against $1"
(cd $(dirname "$0")/../web && MOCHA_URL="$1" pnpm run test)
