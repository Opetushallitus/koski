#!/bin/bash
npx prettier --config web/.prettierrc.json --write src/main/resources/mockdata/eperusteet/**/*.json
npx prettier --config web/.prettierrc.json --write src/main/resources/mockdata/koodisto/**/*.json
npx prettier --config web/.prettierrc.json --write src/main/resources/mockdata/lokalisointi/**/*.json
npx prettier --config web/.prettierrc.json --write src/main/resources/mockdata/organisaatio/**/*.json
