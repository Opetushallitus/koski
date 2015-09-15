#!/bin/bash

# Environment variables DEPLOY_DIR and LOG_DIR are set by the calling hook script.

export JAVA_OPTS="\
-Dtor.profile=cloud \
-Dtor.port=8080 \
-Dlog4j.configuration=file://$DEPLOY_DIR/src/main/resources/log4j.cloud.properties \
-Dlog4j.log.dir=$LOG_DIR"

pkill java
make build && { nohup make run &>> $LOG_DIR/tor.stdout.log & }
