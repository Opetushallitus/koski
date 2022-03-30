#!/bin/bash
set -euo pipefail

function has_validation_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "koski/validation|api/OppijaValidation" > /dev/null
}

function has_validaation_muustohistoria_changes() {
  git log --name-only --pretty=format: HEAD...origin/master | grep -E "validaation_muutoshistoria" > /dev/null
}

function check_changes() {
  if has_validation_changes; then
    if ! has_validaation_muustohistoria_changes; then
      echo "ERROR! Branch has validation changes but no changes to validaation_muutoshistoria.md:"
      git log --name-only HEAD...origin/master
      exit 1
    fi
  fi
}

check_changes
