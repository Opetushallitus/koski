#!/bin/sh
set -euo pipefail

ENV=${1:-}
PACKAGE=${2:-}
CLOUD_ENV_DIR=${CLOUD_ENV_DIR:-}
DIR=$(cd `dirname $0`; pwd)
RC_FILE="$DIR"/cloudrc

VALID_ENVS=(
  "vagrant"
  "tordev"
)

function usage() {
  echo "Usage: `basename $0` <env> <package>"
  echo " where <env> is one of [`echo "${VALID_ENVS[@]}"|sed 's/ / | /g'`]"
  echo "   and <package> is the package to deploy"
  echo
  echo "NB: You need check out the cloud environment repository and set the CLOUD_ENV_DIR environment variable before running this script"
  echo 'eg: export CLOUD_ENV_DIR="$HOME/workspace/oph-poutai-env"'
  echo "Note that you can also add a file $RC_FILE and set the variable there"
  exit 1
}

if [ -f "$RC_FILE" ]; then
  source "$RC_FILE"
fi

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]] || [ ! -f "$PACKAGE" ] || [ -z "$CLOUD_ENV_DIR" ] || [ ! -d "$CLOUD_ENV_DIR" ]; then
  usage
fi

ANSIBLE_ARGS=${ANSIBLE_ARGS:-""}
if [ "$ENV" == "vagrant" ]; then
  ANSIBLE_ARGS="${ANSIBLE_ARGS} --user=vagrant"
fi

cd "$CLOUD_ENV_DIR"
set +u
if [ -z "$OS_USERNAME" ] || [ -z "$OS_PASSWORD" ]; then
  source "$CLOUD_ENV_DIR"/*-openrc.sh
fi
source "$CLOUD_ENV_DIR"/pouta-venv/bin/activate
set -u
export TF_VAR_env="$ENV"
ansible-playbook $ANSIBLE_ARGS --extra-vars=koski_package="$PACKAGE" -i openstack_inventory.py "$DIR"/site.yml
