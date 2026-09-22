#!/bin/bash
set -euo pipefail

# set -e does not fire inside the if-conditions below, so git runs here.
changed_files=$(git log --name-only --pretty=format: origin/master..HEAD)

function has_koski_db_schema_changes() {
  grep --quiet --extended-regexp "src/main/resources/db/migration" <<< "$changed_files"
}

function has_raportointikanta_db_schema_changes() {
  grep --quiet --extended-regexp "src/main/scala/fi/oph/koski/raportointikanta/RaportointiDatabase" <<< "$changed_files"
}

function has_valpas_db_schema_changes() {
  grep --quiet --extended-regexp "src/main/resources/valpas/migration" <<< "$changed_files"
}

function db_docs_updated() {
  local -r name="$1"
  grep --quiet --extended-regexp "documentation/tietokantaskeemat/$name" <<< "$changed_files"
}

function check_changes() {
  if has_koski_db_schema_changes; then
    if ! db_docs_updated "koski"; then
      echo "::error::Branch has a Koski database migration but the database documentation hasn't been updated."
      echo "Start KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi

  if has_valpas_db_schema_changes; then
    if ! db_docs_updated "valpas"; then
      echo "::error::Branch has a Valpas database migration but the database documentation hasn't been updated."
      echo "Start KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi

  if has_raportointikanta_db_schema_changes; then
    if ! db_docs_updated "koski-raportointikanta"; then
      echo "::error::Branch has possible schema changes to raportointikanta but the database documentation hasn't been updated."
      echo "It is possible the changes are cosmetic or not visible to API users, but needs to be checked."
      echo "Fix documentation by starting KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi
}

check_changes
