#!/bin/sh
set -euo pipefail

ENV=${1:-}
PACKAGE=${2:-}
CLOUD_ENV_DIR=${CLOUD_ENV_DIR:-}
RC_FILE=$(dirname $0)/cloudrc

VALID_ENVS=(
  "vagrant"
  "tordev"
)

function usage() {
  echo "Usage: `basename $0` <env> <package>"
  echo " where <env> is one of [`echo ${VALID_ENVS[@]}|sed 's/ / | /g'`]"
  echo "   and <package> is the package to deploy"
  echo
  echo "NB: You need check out the cloud environment repository and set the CLOUD_ENV_DIR environment variable before running this script"
  echo 'eg: export CLOUD_ENV_DIR="$HOME/workspace/oph-poutai-env"'
  echo "Note that you can also add a file $RC_FILE and set the variable there"
  exit 1
}

if [ -f "$RC_FILE" ]; then
  source $RC_FILE
fi

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]] || [ ! -f "$PACKAGE" ] || [ -z "$CLOUD_ENV_DIR" ] || [ ! -d "$CLOUD_ENV_DIR" ]; then
  usage
fi

ANSIBLE_ARGS=""
if [ "$ENV" == "vagrant" ]; then
  ANSIBLE_ARGS="--user=vagrant"
fi

ansible-playbook $ANSIBLE_ARGS --extra-vars=koski_package="$PACKAGE" -i scripts/inventory/$ENV scripts/site.yml
