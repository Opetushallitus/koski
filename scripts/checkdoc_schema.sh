#!/bin/bash
set -euo pipefail

# set -e does not fire inside the if-conditions below, so git runs here.
changed_files=$(git log --name-only --pretty=format: origin/master..HEAD)

function has_schema_changes() {
  grep --quiet --extended-regexp "src/main/scala/fi/oph/koski/schema" <<< "$changed_files"
}

function has_tiedonsiirtoprotokollan_muutoshistoria_changes() {
  grep --quiet --extended-regexp "tiedonsiirtoprotokollan_muutoshistoria" <<< "$changed_files"
}

function check_changes() {
  if has_schema_changes; then
    if ! has_tiedonsiirtoprotokollan_muutoshistoria_changes; then
      echo "::error::Branch has schema changes but no changes to tiedonsiirtoprotokollan_muutoshistoria.md."
      echo "It is possible the changes are cosmetic or not visible to API users, but needs to be checked."
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi
}

check_changes
