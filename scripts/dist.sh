#!/bin/bash
set -euo pipefail

version=${1:-}
BASE_DIR=$(git rev-parse --show-toplevel)

function usage() {
  echo "Usage: `basename $0` <version>"
  echo " where <version> is application's new version"
  exit 1
}

function create_version() {
  mkdir -p $BASE_DIR/target
  if [ "$cleandist" = "true" ]; then
    echo "cleaning dist directory"
    rm -rf $BASE_DIR/target/dist/web && rm -rf $BASE_DIR/target/dist
  fi
  git archive --format=tar --prefix=dist/ HEAD | (cd $BASE_DIR/target && tar xf -)

  if [ "$version" == "local" ]; then
    (cd $BASE_DIR/target/dist && mvn install -DskipTests=true)
  else
    (cd $BASE_DIR/target/dist && mvn versions:set -DnewVersion=$version)
    (cd $BASE_DIR/target/dist && make clean && mvn deploy -DskipTests=true)
    git tag $version
    git push origin $version
  fi
}

if [ -z "$version" ]; then
  usage
fi

if GIT_DIR=$BASE_DIR/.git git show-ref --tags | egrep -q "refs/tags/$1$"
then
    echo "Version already exists. All versions: "
    git tag
    exit 1
else
    create_version
fi
