#!/bin/sh
set -euo pipefail

ENV=${1:-}
VERSION=${2:-}
CLOUD_ENV_DIR=${CLOUD_ENV_DIR:-}
DIR=$(cd `dirname $0`; pwd)
BASE_DIR=$(git rev-parse --show-toplevel)
GROUP_ID="fi/vm/sade"
ARTIFACT_ID="koski"
TMP_APPLICATION="${TMPDIR}${ARTIFACT_ID}-${VERSION}.war"

VALID_ENVS=(
  "vagrant"
  "cloud"
)

function usage() {
  echo "Usage: `basename $0` <env> <version>"
  echo " where <env> is one of [`echo "${VALID_ENVS[@]}"|sed 's/ / | /g'`]"
  echo "   and <version> is the application version to deploy."
  echo "   to deploy a local version use 'local' as version."
  echo
  echo "NB: You need check out the cloud environment repository and set the CLOUD_ENV_DIR environment variable before running this script"
  echo 'eg: export CLOUD_ENV_DIR="$HOME/workspace/koski-env"'
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

INVENTORY=${INVENTORY:-"openstack_inventory.py"}
ANSIBLE_ARGS=${ANSIBLE_ARGS:-""}

if [ -z "$ENV" ] || ! [[ " ${VALID_ENVS[@]} " =~ " ${ENV} " ]]; then
  echo "Missing ENV or invalid ENV"
  usage
fi

if [ -z "$OS_USERNAME" ]; then
  echo "Missing OS_USERNAME"
  usage
fi

if [ -z "$VERSION" ]; then
  echo "Missing VERSION"
  usage
fi

if [ -z "$CLOUD_ENV_DIR" ]; then
  echo "Missing CLOUD_ENV_DIR"
fi

if [ ! -d "$CLOUD_ENV_DIR" ]; then
  echo "CLOUD_ENV_DIR is not a directory"
  usage
fi

set +u
if [ ! -z "$OS_TENANT_NAME" ]; then
  echo "Found Cloud settings for $OS_TENANT_NAME in env"
else
    if [ "$ENV" == "vagrant" ]; then
        ANSIBLE_ARGS="${ANSIBLE_ARGS} --user=vagrant"
        INVENTORY="vagrant/inventory"
    else
        echo Missing OS_TENANT_NAME environment variable and env is not vagrant
        return 1
    fi
fi
set -u

echo "Using inventory $INVENTORY in directory $CLOUD_ENV_DIR"

cd "$CLOUD_ENV_DIR"

download_version

ansible-playbook $ANSIBLE_ARGS --extra-vars=koski_package="${TMPDIR}${ARTIFACT_ID}-${VERSION}.war" -i $INVENTORY "$DIR"/site.yml
