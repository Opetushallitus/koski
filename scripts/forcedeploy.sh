#!/bin/bash -e

if [ $# -eq 0 ]; then
    KOSKI_SERVER=tordev-tor-app
else
    KOSKI_SERVER=$1
fi

date >> README.md
git commit README.md -m "fake commit"
make KOSKI-SERVER=$KOSKI_SERVER deploy
git reset --hard "HEAD^"
