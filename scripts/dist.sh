#!/bin/sh
set -euo pipefail

cd `dirname $0`/..

OUTPUTDIR=${1:-}

if [ -z "$OUTPUTDIR" ]; then
  echo "Usage: `basename $0` <outputdir>"
  exit 1
fi

mkdir -p $OUTPUTDIR
cp -r web target/classes $OUTPUTDIR
cp -r src/main $OUTPUTDIR/src/
mvn dependency:copy-dependencies
cp -r target/dependency $OUTPUTDIR/lib

cd $OUTPUTDIR
zip -qr ../$(basename $OUTPUTDIR).zip *
