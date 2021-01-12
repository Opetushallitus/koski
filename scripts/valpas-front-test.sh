#!/bin/bash
set -eou pipefail
echo "Running Jest tests"
cd $(dirname "$0")/../valpas-web
npm ci
npm run build
npm run test:integration
