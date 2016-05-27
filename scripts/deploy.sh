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
  echo "Usage `basename $0` <env> <package>"
  echo " where <env> is one of [`echo ${VALID_ENVS[@]}|sed 's/ / | /g'`]"
  echo "   and <package> is the package to deploy"
  exit 1
}

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]] || [ ! -f "$PACKAGE" ]; then
  usage
fi

if [ "$ENV" == "vagrant" ]; then
  if [ ! -f "$PRIVATE_KEY" ]; then
    echo "Please set PRIVATE_KEY env variable to point to vagrant private key found under oph-poutai-env repo, and rerun the deploy"
    echo "eg: export PRIVATE_KEY=\"\$HOME/workspace/oph-poutai-env/.vagrant/machines/localcloudbox/virtualbox/private_key\""
    exit 1
  fi
  export ANSIBLE_SSH_ARGS="-o UserKnownHostsFile=/dev/null -o IdentitiesOnly=yes -i '$PRIVATE_KEY' -o ControlMaster=auto -o ControlPersist=60s"
  ansible-playbook -u vagrant --extra-vars=koski_package="$PACKAGE" -i scripts/inventory/$ENV scripts/site.yml
else
  echo not yet implemented
fi
