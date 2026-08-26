#!/bin/bash
set -euo pipefail

MODE="${1:-test}"
REPO="$(cd "$(dirname "$0")/.." && pwd)"
HOST_BACKEND="${2:-${BACKEND_HOST:-http://localhost:7021}}"

case "$MODE" in
  test|update) ;;
  *) echo "Käyttö: $0 [test|update] [backend-url]" >&2; exit 64 ;;
esac

if ! curl -sf -o /dev/null "$HOST_BACKEND/koski/virkailija"; then
  echo "VIRHE: Koski ei vastaa osoitteessa $HOST_BACKEND" >&2
  echo "       Käynnistä sovellus (make run) tai aseta BACKEND_HOST." >&2
  exit 1
fi

pw_version="$(sed -n 's/^  playwright@\([0-9][0-9.]*\):$/\1/p' \
  "$REPO/web/pnpm-lock.yaml" 2>/dev/null | sort -u || true)"
if [ -z "$pw_version" ]; then
  echo "VIRHE: Playwright-version luku epäonnistui (web/pnpm-lock.yaml)." >&2
  exit 1
fi

PINNED_PW_VERSION="1.61.0"
PINNED_PW_DIGEST="sha256:264136758e43332108f6420f82c47f639f619ca65301065ceade677763f477ec"

if [ "$pw_version" != "$PINNED_PW_VERSION" ]; then
  echo "VIRHE: Playwright on nostettu versioon $pw_version, mutta imagen" >&2
  echo "       digest on pinnattu versiolle $PINNED_PW_VERSION." >&2
  echo "       Päivitä PINNED_PW_VERSION ja PINNED_PW_DIGEST tässä skriptissä:" >&2
  echo "       docker buildx imagetools inspect \\" >&2
  echo "         mcr.microsoft.com/playwright:v${pw_version}-jammy" >&2
  exit 1
fi

IMAGE="mcr.microsoft.com/playwright:v${pw_version}-jammy@${PINNED_PW_DIGEST}"

if ! docker info >/dev/null 2>&1; then
  echo "VIRHE: Docker ei ole käynnissä. Visuaalitestit ajetaan kontissa." >&2
  echo "       Ks. documentation/visual-testing.md" >&2
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

echo "Ajetaan visuaalitestit kontissa ($IMAGE), backend: $CONTAINER_BACKEND"

docker run --rm \
  $NET_ARGS \
  -v "$REPO":/work \
  -v koski-visual-node-modules:/work/web/node_modules \
  -w /work/web \
  -e BACKEND_HOST="$CONTAINER_BACKEND" \
  -e CI="${CI:-}" \
  `# Ilman tätä HTML-raportti kirjoittuisi konttiin väärään hakemistoon,` \
  `# eikä CI:n artifaktin lataus löytäisi mitään.` \
  -e PLAYWRIGHT_HTML_REPORT="${PLAYWRIGHT_HTML_REPORT:-}" \
  "$IMAGE" \
  bash -lc "
    set -eu
    corepack enable >/dev/null 2>&1
    pnpm config set store-dir /work/web/node_modules/.pnpm-store >/dev/null 2>&1
    pnpm install --frozen-lockfile >/dev/null
    # Store kasvaa muuten rajatta
    if [ -z \"\${CI:-}\" ]; then
      pnpm store prune >/dev/null 2>&1 || true
    fi
    pnpm exec playwright test $PW_ARGS
  "

if [ "$MODE" = "update" ]; then
  cat <<'OHJE'

Baseline-kuvat nauhoitettu. Tarkista ennen committia:

  git status --short -- web/test/e2e/__screenshots__

  M  = muuttunut kuva. Muuttuivatko vain ne näkymät, joihin kosketit?
  ?? = uusi kuva. Avaa se ja katso, että näkymä on oikea ja kokonaan
       renderöitynyt. (Pelkkä `git diff` ei näytä uusia kuvia.)
OHJE
fi
