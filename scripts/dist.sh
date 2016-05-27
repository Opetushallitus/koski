#!/bin/sh
set -euo pipefail

cd `dirname $0`/..

VERSION=$(git rev-parse --short HEAD)
OUTPUTDIR=${1:-"target/koski-$VERSION"}

mkdir -p $OUTPUTDIR
cp -r web target/classes $OUTPUTDIR
cp -r src/main $OUTPUTDIR/src/
mvn dependency:copy-dependencies
cp -r target/dependency $OUTPUTDIR/lib

cd $OUTPUTDIR
zip -qr ../$(basename $OUTPUTDIR).zip *
