#!/bin/bash
set -euo pipefail

# set -e does not fire inside the if-conditions below, so git runs here.
changed_files=$(git log --name-only --pretty=format: origin/master..HEAD)

function has_validation_changes() {
  grep --quiet --extended-regexp "koski/validation|koski/eperusteetvalidation|api/OppijaValidation" <<< "$changed_files"
}

function has_validaation_muustohistoria_changes() {
  grep --quiet --extended-regexp "validaation_muutoshistoria" <<< "$changed_files"
}

function check_changes() {
  if has_validation_changes; then
    if ! has_validaation_muustohistoria_changes; then
      echo "::error::Branch has validation changes but no changes to validaation_muutoshistoria.md."
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi
}

check_changes
