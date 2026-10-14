#!/usr/bin/env bash
# Demo: aiemmatOpiskeluoikeudet-massaluovutuskysely Helsingin kaupungin oppilaitoksille paikallista Koskea vasten.
# Ajaa kyselyn Stadin ammattiopistolle ja Kulosaaren ala-asteelle niiden nykyisistä demo-oppijoista, lataa tulostiedostot ja
# tulostaa jokaisesta demo-oppijasta kyselyn palauttamat aiemmat opiskeluoikeudet ja niiden alle oppijan kaikki
# opiskeluoikeudet (GET /koski/api/oppija/{oid} pääkäyttäjänä).
#
# Usage: scripts/demo-aiemmat-oo.sh [user] [hakusana]
#   user      kyselyn tekijä, oletus hki-aiemmat-oo (hki-aiemmat-oo-ei-luottamuksellinen näyttää arkaluontoiset tiedot suodatettuina)
#   hakusana  oppijoiden sukunimihaku, oletus "Aiemmat-" (demo-oppijat)
# Tulostiedostot ladataan väliaikaishakemistoon, joka poistetaan lopuksi.
# Vaatii: curl, jq. Mock-käyttäjien salasana on sama kuin tunnus.
set -euo pipefail
USER="${1:-hki-aiemmat-oo}"; HAKU="${2:-Aiemmat-}"
KOSKI="http://localhost:7021/koski/api"
OPPILAITOKSET="1.2.246.562.10.52251087186 1.2.246.562.10.64353470871"  # Stadin ammattiopisto, Kulosaaren ala-aste
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

# 1. Demo-oppijat (sukunimihaku pääkäyttäjänä) ja heidän kaikki opiskeluoikeutensa
OIDIT=$(curl -s -u pää:pää -H 'Content-Type: application/json' -d "{\"query\":\"$HAKU\"}" "$KOSKI/henkilo/search" | jq -r '.["henkilöt"][].oid' | sort)
for OID in $OIDIT; do curl -s -u pää:pää "$KOSKI/oppija/$OID" > "$OUT/$OID.kaikki.json"; done

# 2. Kysely kullekin oppilaitokselle sen nykyisistä opiskelijoista (päättymätön opiskeluoikeus oppilaitoksessa):
#    luonti, odotus ja tulostiedostojen lataus (302 -> presigned S3-osoite, seurataan uudelleenohjausta)
for OPPILAITOS in $OPPILAITOKSET; do
  OPISKELIJAT=$(for OID in $OIDIT; do
    jq -r --arg o "$OPPILAITOS" 'select(any(.opiskeluoikeudet[]; .oppilaitos.oid == $o and .["päättymispäivä"] == null)) | .["henkilö"].oid' "$OUT/$OID.kaikki.json"
  done | jq -R . | jq -sc .)
  [ "$OPISKELIJAT" != "[]" ] || continue
  BODY="{\"type\":\"aiemmatOpiskeluoikeudet\",\"format\":\"application/json\",\"organisaatioOid\":\"$OPPILAITOS\",\"oppijaOidit\":$OPISKELIJAT}"
  RESP=$(curl -sS -u "$USER:$USER" -H 'Content-Type: application/json' -d "$BODY" "$KOSKI/massaluovutus")
  QID=$(echo "$RESP" | jq -r '.queryId // empty'); [ -n "$QID" ] || { echo "Kyselyn luonti epäonnistui:"; echo "$RESP" | jq . 2>/dev/null || echo "$RESP"; exit 1; }
  until STATUS=$(curl -s -u "$USER:$USER" "$KOSKI/massaluovutus/$QID"); [ "$(echo "$STATUS" | jq -r .status)" = complete ]; do
    [ "$(echo "$STATUS" | jq -r .status)" = failed ] && { echo "$STATUS" | jq .; exit 1; }
    sleep 1
  done
  mkdir -p "$OUT/$OPPILAITOS"
  for URL in $(echo "$STATUS" | jq -r '.files[]'); do curl -s -L -u "$USER:$USER" "$URL" -o "$OUT/$OPPILAITOS/$(basename "$URL")"; done
  NIMI=$(jq -r --arg o "$OPPILAITOS" '.opiskeluoikeudet[] | select(.oppilaitos.oid == $o) | .oppilaitos.nimi.fi' "$OUT"/*.kaikki.json | head -1)
  echo "aiemmatOpiskeluoikeudet ($USER), $NIMI: $(echo "$OPISKELIJAT" | jq length) oppijaa, $(ls "$OUT/$OPPILAITOS" | wc -l | tr -d ' ') tulostiedostoa"
done
echo

# 3. Oppijoittain: kyselyn tulos ja kaikki opiskeluoikeudet
for OID in $OIDIT; do
  jq -r '"\(.["henkilö"].etunimet) \(.["henkilö"].sukunimi) (\(.["henkilö"].oid))"' "$OUT/$OID.kaikki.json"
  TULOS=$(ls "$OUT"/*/"$OID.json" 2>/dev/null | head -1 || true)
  if [ -n "$TULOS" ]; then taulukko "Aiemmat opiskeluoikeudet:" < "$TULOS"
  else printf '  Aiemmat opiskeluoikeudet:\n    (ei tulostiedostoa)\n\n'; fi
  taulukko "Kaikki opiskeluoikeudet:" < "$OUT/$OID.kaikki.json"
done
