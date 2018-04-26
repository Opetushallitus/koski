#!/bin/bash
WEEK=$(date "+%V")
HASH=$(echo $WEEK | cat - pom.xml web/package.json web/package-lock.json | shasum | sed 's/ .*//')
PREFIX=/tmp/koski-build-cache
SUFFIX=.tar.gz
FILE=$PREFIX-$HASH$SUFFIX
case "$1" in
    save)
        if [ -e "$FILE" ]; then
          echo "Not overwriting build cache $FILE"
        else
          rm -f "$PREFIX*$SUFFIX"
          echo "Saving build cache to $FILE"
          time tar czf "$FILE" web/node web/node_modules
        fi    
        ;;
    restore)
        if [ -e "$FILE" ]; then
            echo "Restoring build cache from $FILE"
            time tar xzf "$FILE"
        else
            echo "Build cache $FILE does not exist"
        fi
        ;;
    *)
        echo "Usage: jenkins-build-cache.sh save | restore"
        exit 1
        ;;
esac
