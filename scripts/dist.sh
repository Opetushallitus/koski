#!/bin/sh
set -euo pipefail

version=${1:-}
BASE_DIR=$(git rev-parse --show-toplevel)

function usage() {
  echo "Usage: `basename $0` <version>"
  echo " where <version> is application's new version"
  exit 1
}

function buildversiontxt() {
  cat >$BASE_DIR/target/build/web/buildversion.txt <<EOL
artifactId=koski
version=$version
vcsRevision=`git rev-parse HEAD`
EOL
}

function create_version() {
  mkdir -p $BASE_DIR/target && rm -rf $BASE_DIR/target/build && git archive --format=tar --prefix=build/ HEAD | (cd $BASE_DIR/target && tar xf -)
  cp -r $BASE_DIR/web/node_modules $BASE_DIR/target/build/web/ || true
  buildversiontxt

  if [ "$version" == "local" ]; then
    (cd $BASE_DIR/target/build && mvn install -DskipTests=true)
  else
    (cd $BASE_DIR/target/build && mvn versions:set -DnewVersion=$version)
    (cd $BASE_DIR/target/build && mvn clean deploy)
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
