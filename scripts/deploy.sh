#!/bin/sh
set -euo pipefail

ENV=${1:-}
VERSION=${2:-}
CLOUD_ENV_DIR=${CLOUD_ENV_DIR:-}
DIR=$(cd `dirname $0`; pwd)
BASE_DIR=$(git rev-parse --show-toplevel)
RC_FILE="$BASE_DIR"/scripts/cloudrc
GROUP_ID="fi/vm/sade"
ARTIFACT_ID="koski"
TMP_APPLICATION="${TMPDIR}${ARTIFACT_ID}-${VERSION}.war"

VALID_ENVS=(
  "vagrant"
  "tordev"
  "koskiqa"
)

function usage() {
  echo "Usage: `basename $0` <env> <version>"
  echo " where <env> is one of [`echo "${VALID_ENVS[@]}"|sed 's/ / | /g'`]"
  echo "   and <version> is the application version to deploy."
  echo "   to deploy a local version use 'local' as version."
  echo
  echo "NB: You need check out the cloud environment repository and set the CLOUD_ENV_DIR environment variable before running this script"
  echo 'eg: export CLOUD_ENV_DIR="$HOME/workspace/oph-poutai-env"'
  echo "Note that you can also add a file $RC_FILE and set the variable there"
  exit 1
}

function download_version {
  if [ "$VERSION" == "local" ]; then
    DOWNLOAD_URL="file://${HOME}/.m2/repository/${GROUP_ID}/${ARTIFACT_ID}/master-SNAPSHOT/${ARTIFACT_ID}-master-SNAPSHOT.war"
  else
    if [[ "$VERSION" == *SNAPSHOT ]]; then
      DOWNLOAD_ROOT="https://artifactory.oph.ware.fi/artifactory/oph-sade-snapshot-local/${GROUP_ID}/${ARTIFACT_ID}/${VERSION}/"
      WAR_WITH_VERSION=`curl -s ${DOWNLOAD_ROOT} | grep "war\"" | tail -n1 | cut -d \" -f 2`
      DOWNLOAD_URL="${DOWNLOAD_ROOT}${WAR_WITH_VERSION}"
    else
      DOWNLOAD_URL="https://artifactory.oph.ware.fi/artifactory/oph-sade-release-local/${GROUP_ID}/${ARTIFACT_ID}/${VERSION}/${ARTIFACT_ID}-${VERSION}.war"
    fi
  fi
  echo "# Download url: $DOWNLOAD_URL"
  curl -s -S -f -L $DOWNLOAD_URL -o "${TMP_APPLICATION}"
  echo "# Application downloaded to: ${TMP_APPLICATION}"
}

if [ -f "$RC_FILE" ]; then
  source "$RC_FILE"
fi

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]] || [ -z "$VERSION" ] || [ -z "$CLOUD_ENV_DIR" ] || [ ! -d "$CLOUD_ENV_DIR" ]; then
  usage
fi

ANSIBLE_ARGS=${ANSIBLE_ARGS:-""}
INVENTORY=${INVENTORY:-"openstack_inventory.py"}
if [ "$ENV" == "vagrant" ]; then
  ANSIBLE_ARGS="${ANSIBLE_ARGS} --user=vagrant"
  INVENTORY="vagrant/inventory"
fi

cd "$CLOUD_ENV_DIR"
set +u
if [ -z "$OS_USERNAME" ] || [ -z "$OS_PASSWORD" ] && [ "$ENV" != "vagrant" ]; then
  source "$CLOUD_ENV_DIR"/*-openrc.sh
fi

set -u
export TF_VAR_env="$ENV"

download_version

ansible-playbook $ANSIBLE_ARGS --extra-vars=koski_package="${TMPDIR}${ARTIFACT_ID}-${VERSION}.war" -i $INVENTORY "$DIR"/site.yml
