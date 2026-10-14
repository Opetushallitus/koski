#!/usr/bin/env bash
# Demo: aiemmatOpiskeluoikeudet-massaluovutuskysely Stadin ammattiopistolle paikallista Koskea vasten.
# Ajaa kyselyn, lataa tulostiedostot ja tulostaa jokaisesta demo-oppijasta kyselyn palauttamat aiemmat
# opiskeluoikeudet ja niiden alle oppijan kaikki opiskeluoikeudet (GET /koski/api/oppija/{oid} pääkäyttäjänä).
#
# Usage: scripts/demo-aiemmat-oo.sh [user] [hakusana]
#   user      kyselyn tekijä, oletus hki-aiemmat-oo (hki-aiemmat-oo-ei-luottamuksellinen näyttää arkaluontoiset tiedot suodatettuina)
#   hakusana  oppijoiden sukunimihaku, oletus "Aiemmat-" (demo-oppijat)
# Tulostiedostot ladataan väliaikaishakemistoon, joka poistetaan lopuksi.
# Vaatii: curl, jq. Mock-käyttäjien salasana on sama kuin tunnus.
set -euo pipefail
USER="${1:-hki-aiemmat-oo}"; HAKU="${2:-Aiemmat-}"
KOSKI="http://localhost:7021/koski/api"
STADIN="1.2.246.562.10.52251087186"
OUT="$(mktemp -d)"; trap 'rm -rf "$OUT"' EXIT

# jq: oppijan opiskeluoikeudet taulukkona (sama rakenne massaluovutuksen tulostiedostossa ja oppija-API:n vastauksessa)
TAULUKKO='
def pad(n): if length < n then . + (" " * (n - length)) else . end;
$otsikko,
( .opiskeluoikeudet | sort_by(.["alkamispäivä"])[]
  | "  \((.tyyppi.nimi.fi // .tyyppi.koodiarvo) | pad(22))"
    + "  \("\(.oppilaitos.nimi.fi) / \(.koulutustoimija.nimi.fi // "-")" | pad(54))"
    + "  \("\(.["alkamispäivä"]) – \(.["päättymispäivä"] // "jatkuu")" | pad(23))"
    + "  \((.tila.opiskeluoikeusjaksot | last | .tila.koodiarvo) | pad(20))"
    + "  \((.["lisätiedot"] // {}) | keys | join(", "))"
),
""'
taulukko() { jq -r --arg otsikko "$1" "$TAULUKKO" | sed -e 's/^\(.\)/  \1/' -e 's/ *$//'; }

# 1. Kysely demo-oppijoille (sukunimihaku pääkäyttäjänä): luonti, odotus ja tulostiedostojen lataus
#    (302 -> presigned S3-osoite, seurataan uudelleenohjausta)
OIDIT=$(curl -s -u pää:pää -H 'Content-Type: application/json' -d "{\"query\":\"$HAKU\"}" "$KOSKI/henkilo/search" | jq -c '[.["henkilöt"][].oid] | sort')
BODY="{\"type\":\"aiemmatOpiskeluoikeudet\",\"format\":\"application/json\",\"organisaatioOid\":\"$STADIN\",\"oppijaOidit\":$OIDIT}"
RESP=$(curl -sS -u "$USER:$USER" -H 'Content-Type: application/json' -d "$BODY" "$KOSKI/massaluovutus")
QID=$(echo "$RESP" | jq -r '.queryId // empty'); [ -n "$QID" ] || { echo "Kyselyn luonti epäonnistui:"; echo "$RESP" | jq . 2>/dev/null || echo "$RESP"; exit 1; }
until STATUS=$(curl -s -u "$USER:$USER" "$KOSKI/massaluovutus/$QID"); [ "$(echo "$STATUS" | jq -r .status)" = complete ]; do
  [ "$(echo "$STATUS" | jq -r .status)" = failed ] && { echo "$STATUS" | jq .; exit 1; }
  sleep 1
done
for URL in $(echo "$STATUS" | jq -r '.files[]'); do curl -s -L -u "$USER:$USER" "$URL" -o "$OUT/$(basename "$URL")"; done
echo "aiemmatOpiskeluoikeudet ($USER): $(ls "$OUT" | wc -l | tr -d ' ') tulostiedostoa"; echo

# 2. Oppijoittain: kyselyn tulos ja kaikki opiskeluoikeudet
for OID in $(echo "$OIDIT" | jq -r '.[]'); do
  KAIKKI=$(curl -s -u pää:pää "$KOSKI/oppija/$OID")
  echo "$KAIKKI" | jq -r '"\(.["henkilö"].etunimet) \(.["henkilö"].sukunimi) (\(.["henkilö"].oid))"'
  if [ -f "$OUT/$OID.json" ]; then taulukko "Aiemmat opiskeluoikeudet:" < "$OUT/$OID.json"
  else printf '  Aiemmat opiskeluoikeudet:\n    (ei tulostiedostoa)\n\n'; fi
  echo "$KAIKKI" | taulukko "Kaikki opiskeluoikeudet:"
done
