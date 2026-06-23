#!/bin/bash
set -euo pipefail

function has_validation_changes() {
  git log --name-only --pretty=format: origin/master..HEAD | grep -E "koski/validation|koski/eperusteetvalidation|api/OppijaValidation" > /dev/null
}

function has_validaation_muustohistoria_changes() {
  git log --name-only --pretty=format: origin/master..HEAD | grep -E "validaation_muutoshistoria" > /dev/null
}

function check_changes() {
  if has_validation_changes; then
    if ! has_validaation_muustohistoria_changes; then
      echo "ERROR! Branch has validation changes but no changes to validaation_muutoshistoria.md:"
      echo
      git log --name-only origin/master..HEAD
      exit 1
    fi
  fi
}

check_changes
