#!/bin/bash
set -euo pipefail

REPO="$(cd "$(dirname "$0")/.." && pwd)"
HOST_BACKEND="${1:-${BACKEND_HOST:-http://localhost:7021}}"

pw_version="$(sed -n 's/^  playwright@\([0-9][0-9.]*\):$/\1/p' \
  "$REPO/web/pnpm-lock.yaml" 2>/dev/null | sort -u || true)"
if [ -z "$pw_version" ]; then
  echo "VIRHE: Playwright-version luku epäonnistui (web/pnpm-lock.yaml)." >&2
  exit 1
fi

# Sama pinnattu image kuin scripts/koski-visual.sh:ssä - pidä nämä synkassa.
IMAGE="mcr.microsoft.com/playwright:v1.64.1-jammy@sha256:7e9f074b4b69f7c746865936ac7840733cf5dfc957f265a547f331e7b83922d7"
image_tag="${IMAGE#*:v}"
image_tag="${image_tag%%@*}"
image_version="${image_tag%%-*}"
image_variant="${image_tag#"$image_version"}"

if [ "$pw_version" != "$image_version" ]; then
  echo "VIRHE: Playwright on nostettu versioon $pw_version, mutta imagen" >&2
  echo "       digest on pinnattu versiolle $image_version." >&2
  echo "       Päivitä IMAGE-määrityksen versio ja digest scripts/koski-visual.sh:ssä ja tässä." >&2
  exit 1
fi

if ! curl -sf -o /dev/null "$HOST_BACKEND/koski/virkailija"; then
  echo "VIRHE: Koski ei vastaa osoitteessa $HOST_BACKEND" >&2
  echo "       Käynnistä sovellus (make run) tai aseta BACKEND_HOST." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "VIRHE: Docker ei ole käynnissä." >&2
  exit 1
fi

if [ ! -x "$REPO/web/node_modules/.bin/playwright" ]; then
  echo "VIRHE: web/node_modules puuttuu. Aja ensin: cd web && pnpm install" >&2
  exit 1
fi

if [ "$(uname -s)" = "Linux" ]; then
  NET_ARGS="--network host"
  CONTAINER_BACKEND="$HOST_BACKEND"
else
  NET_ARGS="--add-host=host.docker.internal:host-gateway"
  CONTAINER_BACKEND="http://host.docker.internal:${HOST_BACKEND##*:}"
fi

USER_ARGS=(--user "$(id -u):$(id -g)")
runtime_version="$(docker --version)"
if [[ "$runtime_version" == *[Pp]odman* ]]; then
  podman_rootless="$(docker info --format '{{.Host.Security.Rootless}}')"
  if [[ "$podman_rootless" == true ]]; then
    USER_ARGS+=(--userns=keep-id)
  fi
fi

echo "Ajetaan lukio 2019 vanha-vs-uusi-vertailu kontissa ($IMAGE), backend: $CONTAINER_BACKEND"
echo "Testi epäonnistuu tarkoituksella - avaa HTML-raportti vertaillaksesi kuvia."

# Kirjautumisistunnot (test/.pw-state) on keksejä, jotka on sidottu siihen
# hostiin ($HOST_BACKEND), jota vasten ne luotiin (esim. localhost:7021 vs.
# host.docker.internal:7021). Istunto "on voimassa" 20 min riippumatta
# hostista, joten edellisen - mahdollisesti eri hostia vasten luodun - ajon
# istunto hyväksytään sellaisenaan ja näyttää kirjautuneelta, vaikka selain ei
# lähetä väärän hostin keksiä. Lopputulos: fixtures.reset() epäonnistuu
# /koski/login-uudelleenohjauksen takia. Poistetaan siis aina tuoreeltaan.
rm -rf "$REPO/web/test/.pw-state"

set +e
docker run --rm \
  $NET_ARGS \
  "${USER_ARGS[@]}" \
  -v "$REPO":/work \
  -w /work/web \
  -e BACKEND_HOST="$CONTAINER_BACKEND" \
  "$IMAGE" \
  node_modules/.bin/playwright test --config playwright.lukio-vertailu.config.ts
set -e

echo
echo "Avataan raportti selaimessa (Ctrl-C lopettaa raporttipalvelimen)..."
cd "$REPO/web"
exec node_modules/.bin/playwright show-report
