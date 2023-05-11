#!/bin/bash
set -euo pipefail

function has_koski_db_schema_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "src/main/resources/db/migration" > /dev/null
}

function has_raportointikanta_db_schema_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "src/main/scala/fi/oph/koski/raportointikanta/RaportointiDatabase" > /dev/null
}

function has_valpas_db_schema_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "src/main/resources/valpas/migration" > /dev/null
}

function db_docs_updated() {
  local -r name="$1"
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "documentation/tietokantaskeemat/$name" > /dev/null
}

function check_changes() {
  if has_koski_db_schema_changes; then
    if ! db_docs_updated "koski"; then
      echo "ERROR! Branch has a database migration but the database documentation hasn't been updated."
      echo "Start KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only HEAD...origin/master
      exit 1
    fi
  fi

  if has_valpas_db_schema_changes; then
    if ! db_docs_updated "valpas"; then
      echo "ERROR! Branch has a database migration but the database documentation hasn't been updated."
      echo "Start KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only HEAD...origin/master
      exit 1
    fi
  fi

  if has_raportointikanta_db_schema_changes; then
    if ! db_docs_updated "koski-raportointikanta"; then
      echo "ERROR! Branch has possible schema changes to raportointikanta but the database documentation hasn't been updated."
      echo "It is possible the changes are cosmetic or not visible to API users, but needs to be checked."
      echo "Fix documentation by starting KOSKI locally and run 'make db-docs'"
      echo
      git log --name-only HEAD...origin/master
      exit 1
    fi
  fi
}

check_changes
