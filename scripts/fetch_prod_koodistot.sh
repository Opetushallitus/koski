#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR="$SCRIPT_DIR/.."

KOODISTOT_SCALA="$ROOT_DIR/src/main/scala/fi/oph/koski/koodisto/Koodistot.scala"
KOODIT_DIR="$ROOT_DIR/src/main/resources/mockdata/koodisto/koodit"
KOODISTOT_DIR="$ROOT_DIR/src/main/resources/mockdata/koodisto/koodistot"

SKIP_KOODISTOT=(
    "koulutus"
    "oppilaitosnumero"
    "osaamisala"
    "tutkinnonosat"
    "tutkintonimikkeet"
    "moduulikoodistolops2021"
    "opintokokonaisuudet"
)

SKIP_KOODISTOT_JSON=$(IFS=$'\n'; echo "${SKIP_KOODISTOT[*]}" | jq -Rn '[inputs]')

KOODISTOT=$(
    < "$KOODISTOT_SCALA" \
    sed -n 's/^.*KoodistoAsetus("\(.*\)".*$/\1/p' |\
    jq -Rn "([inputs] | unique) - $SKIP_KOODISTOT_JSON | sort" |\
    jq -r '.[]'
)

# shellcheck disable=SC2206
KOODISTOT_ARR=($KOODISTOT)

for KOODISTO in "${KOODISTOT_ARR[@]}"; do
    VERSIO=$(curl -s "https://virkailija.opintopolku.fi/koodisto-service/rest/codes/$KOODISTO" | jq -r '.latestKoodistoVersio.versio')
    echo "Päivitetään $KOODISTO/$VERSIO..."
    curl -s "https://virkailija.opintopolku.fi/koodisto-service/rest/codes/$KOODISTO/$VERSIO" | jq . > "$KOODISTOT_DIR/$KOODISTO.json"
    curl -s "https://virkailija.opintopolku.fi/koodisto-service/rest/codeelement/codes/withrelations/$KOODISTO/$VERSIO" | jq . > "$KOODIT_DIR/$KOODISTO.json"
done
