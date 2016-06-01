#!/bin/sh
set -euo pipefail

ENV=${1:-}
PACKAGE=${2:-}
PRIVATE_KEY=${PRIVATE_KEY:-}

VALID_ENVS=(
  "vagrant"
  "qa"
  "production"
)

function usage() {
  echo "Usage: `basename $0` <env> <package>"
  echo " where <env> is one of [`echo ${VALID_ENVS[@]}|sed 's/ / | /g'`]"
  echo "   and <package> is the package to deploy"
  exit 1
}

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]] || [ ! -f "$PACKAGE" ]; then
  usage
fi

ANSIBLE_ARGS=""
if [ "$ENV" == "vagrant" ]; then
  ANSIBLE_ARGS="--user=vagrant"
fi

ansible-playbook $ANSIBLE_ARGS --extra-vars=koski_package="$PACKAGE" -i scripts/inventory/$ENV scripts/site.yml
