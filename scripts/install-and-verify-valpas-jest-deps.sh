#!/bin/bash
set -eou pipefail

echo "Tarkasteteaan exportatut typescript-tyypityksety..."
cd $(dirname "$0")/..
make ts-types
if [ -n "$(git status --porcelain=v1 2>/dev/null)" ]
then
    echo "ERROR: Skeema on muuttunut, mutta tyypityksiä ei ole päivitetty ajamalla komento make ts-types"
    exit 1
fi

echo "Verifying test environment..."
cd valpas-web
npm ci
npm run test:integration -- -t "Testiympäristön oikeellisuus"


cd ../web
npm ci
npm run build:prod
