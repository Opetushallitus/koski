#!/bin/bash
set -euo pipefail

function has_schema_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "src/main/scala/fi/oph/koski/schema" > /dev/null
}

function has_tiedonsiirtoprotokollan_muutoshistoria_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "tiedonsiirtoprotokollan_muutoshistoria" > /dev/null
}

function check_changes() {
  if has_schema_changes; then
    if ! has_tiedonsiirtoprotokollan_muutoshistoria_changes; then
      echo "ERROR! Branch has schema changes but no changes to tiedonsiirtoprotokollan_muutoshistoria.md."
      echo "It is possible the changes are cosmetic or not visible to API users, but needs to be checked."
      echo
      git log --name-only HEAD...origin/master
      exit 1
    fi
  fi
}

check_changes
