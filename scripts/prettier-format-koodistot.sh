#!/bin/bash
set -euo pipefail

# Prettier-muotoilee mockdatan JSON-tiedostot.
#
# Oletuksena kirjoittaa muutokset (--write). `--check` vain tarkistaa
# muotoilun; sitä käyttävät `make lint` ja CI:n `pnpm run prettier:check`.
#
# Globit on lainausmerkeissä, jotta Prettier tekee laajennuksen itse. Ilman
# lainausmerkkejä bash laajentaa ne, ja koska globstar ei ole päällä, `**`
# vastaa yhtä hakemistotasoa — jolloin suoraan hakemistossa olevat tiedostot
# (eperusteet/, lokalisointi/) jäivät kokonaan käsittelemättä.

ROOT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )/.." &> /dev/null && pwd )
cd "$ROOT_DIR"

MODE="--write"
if [ "${1:-}" = "--check" ]; then
  MODE="--check"
fi

PRETTIER="$ROOT_DIR/node_modules/.bin/prettier"
if [ ! -x "$PRETTIER" ]; then
  pnpm install --frozen-lockfile
fi
"$PRETTIER" --version >/dev/null

"$PRETTIER" "$MODE" \
  "src/main/resources/mockdata/eperusteet/**/*.json" \
  "src/main/resources/mockdata/koodisto/**/*.json" \
  "src/main/resources/mockdata/lokalisointi/**/*.json" \
  "src/main/resources/mockdata/organisaatio/**/*.json"
