#!/bin/bash
set -eou pipefail
echo $PATH
echo "Running mocha tests against $1"
(cd $(dirname "$0")/../web && MOCHA_URL="$1" npm run test)
