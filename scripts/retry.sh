#!/bin/bash
set -o errexit -o nounset -o pipefail

if [ $# -lt 4 ] || [ "$3" != "--" ]; then
  echo "Usage: retry.sh <description> <timeout per attempt> -- <command> [args...]" >&2
  exit 2
fi
description=$1
attempt_timeout=$2
shift 3

delays=(15 45 90)
for attempt in {1..4}; do
  timeout --kill-after=10s "$attempt_timeout" "$@" && exit 0
  status=$?
  if [ "$attempt" -eq 4 ]; then
    echo "::error::$description failed after 4 attempts (exit $status)"
    exit "$status"
  fi
  delay=$(( delays[attempt - 1] + RANDOM % 10 ))
  echo "::warning::$description failed (attempt $attempt/4, exit $status), retrying in ${delay}s"
  sleep "$delay"
done
