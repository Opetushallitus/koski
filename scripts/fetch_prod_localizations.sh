#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR="$SCRIPT_DIR/.."
RESOURCES_DIR="$ROOT_DIR/src/main/resources"

function load_and_format() {
    local -r CATEGORY="$1"
    local -r LOCALIZATION_FILE="$2"
    local -r DEFAULT_TEXTS_FILE="$3"

    cd "$ROOT_DIR" || exit
    curl "https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=$CATEGORY" \
        | jq 'map( . * { createdBy: "anonymousUser", modifiedBy: "anonymousUser" } )' \
        > "$LOCALIZATION_FILE"
    npx prettier --config "$ROOT_DIR/web/.prettierrc.json" --write "$LOCALIZATION_FILE"
    jq '[.[] | select(.locale | contains("fi"))] | map( { (.key): .value } ) | add' < "$LOCALIZATION_FILE" > "$DEFAULT_TEXTS_FILE"
}


load_and_format koski "$RESOURCES_DIR/mockdata/lokalisointi/koski.json" "$RESOURCES_DIR/localization/koski-default-texts.json"
load_and_format valpas "$RESOURCES_DIR/valpas/mockdata/lokalisointi/valpas.json" "$RESOURCES_DIR/valpas/localization/valpas-default-texts.json"
