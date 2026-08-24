#!/bin/bash

set -euo pipefail

# Refreshes the localization texts from the production Lokalisointipalvelu.
#
# Note that this regenerates the *-default-texts.json files too, not just the
# mockdata. A key added there by hand but not yet pushed to Lokalisointipalvelu
# is silently deleted by the next run. The diff runs to tens of thousands of
# lines, so compare the key sets programmatically afterwards and restore
# anything the code still references.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR="$SCRIPT_DIR/.."
RESOURCES_DIR="$ROOT_DIR/src/main/resources"

function load_and_format() {
    local -r CATEGORY="$1"
    local -r LOCALIZATION_FILE="$2"
    local -r DEFAULT_TEXTS_FILE="$3"

    cd "$ROOT_DIR" || exit
    # jq -S pins a canonical key order. Lokalisointipalvelu has returned its
    # fields in a different order on every past refresh, which rewrote the whole
    # mockdata file and made it conflict with any unrelated edit to that file.
    curl "https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=$CATEGORY" \
        | jq -S 'map( . * { createdBy: "anonymousUser", modifiedBy: "anonymousUser" } )' \
        > "$LOCALIZATION_FILE"
    npx prettier --config "$ROOT_DIR/web/.prettierrc.json" --write "$LOCALIZATION_FILE"
    jq '[.[] | select(.locale | contains("fi"))] | map( { (.key): .value } ) | add' < "$LOCALIZATION_FILE" > "$DEFAULT_TEXTS_FILE"
}


load_and_format koski "$RESOURCES_DIR/mockdata/lokalisointi/koski.json" "$RESOURCES_DIR/localization/koski-default-texts.json"
load_and_format valpas "$RESOURCES_DIR/valpas/mockdata/lokalisointi/valpas.json" "$RESOURCES_DIR/valpas/localization/valpas-default-texts.json"
