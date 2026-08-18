#!/bin/bash
set -euo pipefail

# Prettier-muotoilee mockdatan JSON-tiedostot.
#
# Oletuksena kirjoittaa muutokset (--write). `--check` vain tarkistaa
# muotoilun; sitä käyttää `make lint`, joka ajetaan CI:ssä.
#
# Globit on lainausmerkeissä, jotta Prettier tekee laajennuksen itse. Ilman
# lainausmerkkejä bash laajentaa ne, ja koska globstar ei ole päällä, `**`
# vastaa yhtä hakemistotasoa — jolloin suoraan hakemistossa olevat tiedostot
# (eperusteet/, lokalisointi/) jäivät kokonaan käsittelemättä.

MODE="--write"
if [ "${1:-}" = "--check" ]; then
  MODE="--check"
fi

npx prettier --config web/.prettierrc.json "$MODE" \
  "src/main/resources/mockdata/eperusteet/**/*.json" \
  "src/main/resources/mockdata/koodisto/**/*.json" \
  "src/main/resources/mockdata/lokalisointi/**/*.json" \
  "src/main/resources/mockdata/organisaatio/**/*.json"
