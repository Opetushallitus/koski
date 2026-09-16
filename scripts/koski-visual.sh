#!/bin/bash
set -euo pipefail

MODE="${1:-test}"
REPO="$(cd "$(dirname "$0")/.." && pwd)"
HOST_BACKEND="${2:-${BACKEND_HOST:-http://localhost:7021}}"

case "$MODE" in
  test|update) ;;
  *) echo "Käyttö: $0 [test|update] [backend-url]" >&2; exit 64 ;;
esac

pw_version="$(sed -n 's/^  playwright@\([0-9][0-9.]*\):$/\1/p' \
  "$REPO/web/pnpm-lock.yaml" 2>/dev/null | sort -u || true)"
if [ -z "$pw_version" ]; then
  echo "VIRHE: Playwright-version luku epäonnistui (web/pnpm-lock.yaml)." >&2
  exit 1
fi

IMAGE="mcr.microsoft.com/playwright:v1.63.0-jammy@sha256:167d0506cfbe3c294fb214b2d11737326eeee028aa611fa1ba538e5057675847"
image_tag="${IMAGE#*:v}"                      # image:v1.2.3-variant@digest → 1.2.3-variant@digest
image_tag="${image_tag%%@*}"                  # 1.2.3-variant@digest → 1.2.3-variant
image_version="${image_tag%%-*}"              # 1.2.3-variant → 1.2.3
image_variant="${image_tag#"$image_version"}" # 1.2.3-variant → -variant

if [ "$pw_version" != "$image_version" ]; then
  echo "VIRHE: Playwright on nostettu versioon $pw_version, mutta imagen" >&2
  echo "       digest on pinnattu versiolle $image_version." >&2
  echo "       Päivitä IMAGE-määrityksen versio ja digest tässä skriptissä:" >&2
  echo "       docker buildx imagetools inspect \\" >&2
  echo "         mcr.microsoft.com/playwright:v${pw_version}${image_variant}" >&2
  exit 1
fi

if ! curl -sf -o /dev/null "$HOST_BACKEND/koski/virkailija"; then
  echo "VIRHE: Koski ei vastaa osoitteessa $HOST_BACKEND" >&2
  echo "       Käynnistä sovellus (make run) tai aseta BACKEND_HOST." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "VIRHE: Docker ei ole käynnissä. Visuaalitestit ajetaan kontissa." >&2
  echo "       Ks. documentation/visual-testing.md" >&2
  exit 1
fi

# Kontti käyttää hostin web/node_modulesia: testit tarvitsevat Playwrightin
# lisäksi sovelluksen riippuvuuksia. Selaimet tulevat imagesta.
if [ ! -x "$REPO/web/node_modules/.bin/playwright" ]; then
  echo "VIRHE: web/node_modules puuttuu. Aja ensin: cd web && pnpm install" >&2
  exit 1
fi

PW_ARGS="--config playwright.visual.config.ts"
if [ "$MODE" = "update" ]; then
  PW_ARGS="$PW_ARGS --update-snapshots"
fi

# Verkotus: CI:ssä (Linux) backend on samassa hostissa, jolloin --network host
# on yksinkertaisin. Docker Desktopilla (macOS/Windows) se ei toimi, joten
# siellä käytetään host.docker.internal-nimeä.
if [ "$(uname -s)" = "Linux" ]; then
  NET_ARGS="--network host"
  CONTAINER_BACKEND="$HOST_BACKEND"
else
  NET_ARGS="--add-host=host.docker.internal:host-gateway"
  CONTAINER_BACKEND="http://host.docker.internal:${HOST_BACKEND##*:}"
fi

# Ajetaan hostin käyttäjänä, jotta kontin kirjoittamat tiedostot (test-results,
# raportti, baseline-kuvat) eivät jää Linuxilla root-omisteisiksi.
USER_ARGS=(--user "$(id -u):$(id -g)")
runtime_version="$(docker --version)"
if [[ "$runtime_version" == *[Pp]odman* ]]; then
  podman_rootless="$(docker info --format '{{.Host.Security.Rootless}}')"
  if [[ "$podman_rootless" == true ]]; then
    # Rootless Podmanissa pelkkä --user ei säilytä hostin käyttäjätunnusta.
    USER_ARGS+=(--userns=keep-id)
  fi
fi

echo "Ajetaan visuaalitestit kontissa ($IMAGE), backend: $CONTAINER_BACKEND"

docker run --rm \
  $NET_ARGS \
  "${USER_ARGS[@]}" \
  -v "$REPO":/work \
  -w /work/web \
  -e BACKEND_HOST="$CONTAINER_BACKEND" \
  -e CI="${CI:-}" \
  `# Ilman tätä HTML-raportti kirjoittuisi konttiin väärään hakemistoon,` \
  `# eikä CI:n artifaktin lataus löytäisi mitään.` \
  -e PLAYWRIGHT_HTML_REPORT="${PLAYWRIGHT_HTML_REPORT:-}" \
  "$IMAGE" \
  node_modules/.bin/playwright test $PW_ARGS

if [ "$MODE" = "update" ]; then
  cat <<'OHJE'

Baseline-kuvat nauhoitettu. Tarkista ennen committia:

  git status --short -- web/test/e2e/__screenshots__

  M  = muuttunut kuva. Muuttuivatko vain ne näkymät, joihin kosketit?
  ?? = uusi kuva. Avaa se ja katso, että näkymä on oikea ja kokonaan
       renderöitynyt. (Pelkkä `git diff` ei näytä uusia kuvia.)
OHJE
fi
