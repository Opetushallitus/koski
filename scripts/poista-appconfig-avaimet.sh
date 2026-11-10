#!/usr/bin/env bash
set -euo pipefail

# Poistaa AppConfig-tiedostosta annetut avaimet, joita koodi ei lue. Muut avaimet, arvot ja kommentit
# säilyvät, ja tiedosto järjestetään kuten yhdista-appconfig.sh sen järjestää. Avain, jota tiedostossa ei
# ole, vain listataan, joten samat avaimet voi antaa jokaiselle ympäristölle. Avainta, joka on vielä
# reference.confissa, ei poisteta.
#
# Esimerkki, kun koski-aws-infra on Kosken vieressä:
#   ../koski-aws-infra/tools/koskiConfig.sh --env qa > koski-qa.conf
#   scripts/poista-appconfig-avaimet.sh koski-qa.conf koski-qa-uusi.conf piwik.siteId features.kelaui
#   ../koski-aws-infra/tools/koskiConfig.sh --env qa --upload koski-qa-uusi.conf

if [[ $# -lt 3 || ! -f "$1" ]]; then
  echo "Käyttö: $0 APPCONFIG-TIEDOSTO UUSI-TIEDOSTO AVAIN..." >&2
  exit 1
fi
INPUT=$(realpath "$1")
OUTPUT="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"
ARGS="'$INPUT' '$OUTPUT'"
for AVAIN in "${@:3}"; do
  if [[ "$AVAIN" == *"'"* ]]; then
    echo "Avaimessa ei saa olla heittomerkkiä: $AVAIN" >&2
    exit 1
  fi
  ARGS+=" '$AVAIN'"
done

cd "$(dirname "$0")/.."
# exec.skip ohittaa frontendin buildin, mutta se ohittaisi myös exec:javan, joten käännös ajetaan erikseen.
./mvnw --batch-mode --quiet compile -Dexec.skip=true
./mvnw --batch-mode --quiet exec:java -Dexec.mainClass=fi.oph.koski.config.AppConfigAvaintenPoisto \
  -Dexec.args="$ARGS"
