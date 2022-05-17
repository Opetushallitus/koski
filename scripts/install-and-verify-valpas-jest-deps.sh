#!/bin/bash
set -eou pipefail
echo "Verifying test environment..."
cd $(dirname "$0")/../valpas-web
npm ci
npm run test:integration -- -t "Testiympäristön oikeellisuus"

cd ../web
npm ci
npm run build:prod
