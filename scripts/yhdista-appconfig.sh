#!/usr/bin/env bash
set -euo pipefail

# Täydentää AppConfig-tiedoston Kosken reference.confin avaimilla ja järjestää avaimet reference.confin
# järjestykseen {}-lohkoihin. AppConfigin arvot ja avaimiin kiinnitetyt kommentit säilyvät. Puuttuva avain
# saa reference.confin oletuksen. Paikalliselta näyttävät oletukset (mock, localhost...) listataan
# tarkistettaviksi.
#
# Esimerkki, kun koski-aws-infra on Kosken vieressä:
#   ../koski-aws-infra/tools/koskiConfig.sh --env qa > koski-qa.conf
#   scripts/yhdista-appconfig.sh koski-qa.conf koski-qa-uusi.conf
#   ../koski-aws-infra/tools/koskiConfig.sh --env qa --upload koski-qa-uusi.conf

if [[ $# -ne 2 || ! -f "$1" ]]; then
  echo "Käyttö: $0 APPCONFIG-TIEDOSTO UUSI-TIEDOSTO" >&2
  exit 1
fi
INPUT=$(realpath "$1")
OUTPUT="$(cd "$(dirname "$2")" && pwd)/$(basename "$2")"

cd "$(dirname "$0")/.."
# exec.skip ohittaa frontendin buildin, mutta se ohittaisi myös exec:javan, joten käännös ajetaan erikseen.
./mvnw --batch-mode --quiet compile -Dexec.skip=true
./mvnw --batch-mode --quiet exec:java -Dexec.mainClass=fi.oph.koski.config.AppConfigYhdistys \
  -Dexec.args="'$INPUT' '$OUTPUT'"
