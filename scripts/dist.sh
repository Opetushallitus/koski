#!/bin/sh
set -euo pipefail

cd `dirname $0`/..

DIST_DIR=${1:-}

if [ -z "$DIST_DIR" ]; then
  echo "Usage: `basename $0` <outputdir>"
  exit 1
fi

mkdir -p $DIST_DIR/src/main
cp -r web $DIST_DIR/
cp -r src/main/{resources,webapp} $DIST_DIR/src/main/

cd $DIST_DIR
zip -qr ../$(basename $DIST_DIR).zip *
