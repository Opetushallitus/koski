#!/bin/bash
set -euo pipefail

version=${1:-}

function usage() {
  echo "Usage: $(basename $0) <version>"
  echo " where <version> is application's new version"
  exit 1
}

if [ -z "$version" ]; then
  usage
fi

mvn versions:set -DnewVersion="$version"
mvn deploy --batch-mode -DskipTests=true -Dmaven.skip.install=true -DaltDeploymentRepository=github::default::https://maven.pkg.github.com/Opetushallitus/koski
