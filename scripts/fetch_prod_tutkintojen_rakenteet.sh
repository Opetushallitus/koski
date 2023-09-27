#!/bin/bash

set -euo pipefail

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR="$SCRIPT_DIR/.."
RESOURCES_DIR="$ROOT_DIR/src/main/resources"

NAME_PATTERN=${1:-rakenne-*.json}
RAKENTEET_PATH="$RESOURCES_DIR/mockdata/eperusteet/$NAME_PATTERN"

function validaatio() {
    jq -r '{ "voimassaoloAlkaa": .voimassaoloAlkaa, "voimassaoloLoppuu": .voimassaoloLoppuu, "siirtymaPaattyy": .siirtymaPaattyy, "diaarinumero": .diaarinumero }' < "$1"
}

for RAKENNE in $RAKENTEET_PATH; do
    BASENAME=$(basename "$RAKENNE")
    git restore "$RAKENNE"
    
    DIAARINUMERO=$(jq -r .diaarinumero < "$RAKENNE")

    if [ "$DIAARINUMERO" != "null" ]; then
        ID=$(jq -r .id < "$RAKENNE")
        ALKUPERAINEN=$(validaatio "$RAKENNE")

        DATA=$(curl -s "https://eperusteet.opintopolku.fi/eperusteet-service/api/external/peruste/$ID" | jq .)
        KOODI=$(echo "$DATA" | jq -r .koodi)
        if [ "$KOODI" != "null" ]; then
        echo "Skip $BASENAME: Tunnisteella $ID ei löytynyt rakennetta"
        else
        echo "OK   $BASENAME: $DIAARINUMERO (id: $ID)"
        PATCH="$RAKENNE.patch"
        if [ -f "$PATCH" ]; then
            echo "     Tallennetaan muutoksien $(basename "$PATCH") kanssa"
            echo "$DATA" | jq "$(cat "$PATCH")" > "$RAKENNE"
        else
            echo "$DATA" > "$RAKENNE"
        fi

        UUSI=$(validaatio "$RAKENNE")
        
        if [ "$ALKUPERAINEN" != "$UUSI" ]; then
            echo "     VAROITUS: Testien kannalta mahdollisesti oleellisia kenttiä muuttui:"
            echo ""
            echo "     Alkuperäinen: $ALKUPERAINEN"
            echo "     Uusi: $UUSI"
            echo ""
        fi

    fi

    else
      echo "Skip $BASENAME: Ei diaarinumeroa"
    fi
done