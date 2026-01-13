#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
backend_host="${KOSKI_BACKEND_HOST:-http://$(node "$ROOT_DIR/scripts/getmyip.js"):7021}"
cert_base="$ROOT_DIR/testca"

# Prefer Java 23 (required for --release 21), fall back to existing JAVA_HOME or system default
if [ -n "${JAVA_HOME_23_X64:-}" ]; then
  export JAVA_HOME="$JAVA_HOME_23_X64"
elif [ -z "${JAVA_HOME:-}" ] && command -v /usr/libexec/java_home &> /dev/null; then
  export JAVA_HOME="$(/usr/libexec/java_home)"
fi
if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
fi

# Fail fast if Java < 21
JAVA_MAJOR="$(java -version 2>&1 | head -n1 | awk -F[\\\".] '{print $2}')"
if [ -z "$JAVA_MAJOR" ] || [ "$JAVA_MAJOR" -lt 21 ]; then
  echo "Java 21+ is required (found $JAVA_MAJOR). Please install JDK 21+ or set JAVA_HOME_23_X64." >&2
  exit 1
fi

cd "$ROOT_DIR/../java"
JOD_KOSKI_CLIENT_CERT="${JOD_KOSKI_CLIENT_CERT:-$cert_base/certs/client.crt}" \
JOD_KOSKI_CLIENT_KEY="${JOD_KOSKI_CLIENT_KEY:-$cert_base/private/client.key}" \
JOD_KOSKI_SERVER_CERT="${JOD_KOSKI_SERVER_CERT:-$cert_base/certs/root-ca.crt}" \
KOSKI_BACKEND_HOST="$backend_host" \
  ./mvnw spring-boot:run
