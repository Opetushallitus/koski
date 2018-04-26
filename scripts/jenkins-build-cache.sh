#!/bin/bash
WEEK=$(date "+%V")
HASH=$(echo $WEEK | cat - pom.xml web/package.json web/package-lock.json | openssl sha256)
PREFIX=/tmp/koski-build-cache
FILE=$PREFIX-${HASH}.tar
case "$1" in
    save)
        if [ -e "$FILE" ]; then
          echo "Not overwriting build cache $FILE"
        else
          rm -f "$PREFIX*.tar"
          echo "Saving build cache to $FILE"
          tar cf "$FILE" web/node web/node_modules
        fi    
        ;;
    restore)
        if [ -e "$FILE" ]; then
            echo "Restoring build cache from $FILE"
            tar xf "$FILE"
        else
            echo "Build cache $FILE does not exist"
        fi
        ;;
    *)
        echo "Usage: jenkins-build-cache.sh save | restore"
        exit 1
        ;;
esac
