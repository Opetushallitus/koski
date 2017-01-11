#!/bin/bash

ROOT_DIR=`pwd`
RUN_DIR=$ROOT_DIR/target/dist/target/koski-master-SNAPSHOT
KOSKI_CONFIG=${KOSKI_CONFIG:-$ROOT_DIR/src/main/resources/reference.conf}

JAVA_OPTS="\
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.port=5555 \
-Dcom.sun.management.jmxremote.rmi.port=5555 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=localhost \
-Dkoski.port=8080 \
-Dlog4j.configuration=file://$ROOT_DIR/src/main/resources/log4j.properties \
-Dlog4j.log.dir=$ROOT_DIR/log \
-Dconfig.file=$KOSKI_CONFIG \
-Dfile.encoding=UTF-8 \
-Dresourcebase=$RUN_DIR \
"

JAR=`ls $RUN_DIR/WEB-INF/lib/koski*`
java $JAVA_OPTS -classpath "$JAR:$RUN_DIR/WEB-INF/lib/*" "fi.oph.koski.jettylauncher.JettyLauncher"