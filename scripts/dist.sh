#!/bin/sh
set -euo pipefail

version=${1:-}
BASE_DIR=$(git rev-parse --show-toplevel)

function usage() {
  echo "Usage: `basename $0` <version>"
  echo " where <version> is application's new version"
  exit 1
}

function create_version() {
  echo "create version"
  (cd $BASE_DIR && mvn versions:set -DnewVersion=$version)
  (cd $BASE_DIR && mvn clean deploy)
  git tag $version
  #git push origin $version
}

if [ -z "$version" ]; then
  usage
fi

if GIT_DIR=$BASE_DIR/.git git show-ref --tags | egrep -q "refs/tags/$1$"
then
    echo "Version already exists. All versions: "
    git tag
else
    create_version
fi