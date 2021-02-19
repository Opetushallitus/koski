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
    rm -rf $BASE_DIR/target/dist && rm -rf $BASE_DIR/valpas-web/dist
  fi
  git archive --format=tar --prefix=dist/ HEAD | (cd $BASE_DIR/target && tar xf -)

  if [ "$version" == "local" ]; then
    (cd $BASE_DIR/target/dist && mvn install -DskipTests=true)
  else
    (cd $BASE_DIR/target/dist && mvn versions:set -DnewVersion=$version)
    (cd $BASE_DIR/target/dist && make clean && mvn deploy -DskipTests=true)
  fi
}

if [ -z "$version" ]; then
  usage
fi

create_version
