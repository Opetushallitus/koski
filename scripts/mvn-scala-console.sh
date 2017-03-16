#!/bin/bash

mvn test-compile

CLASSPATH=$((find . -type d -name \*classes\*; mvn dependency:list -DoutputAbsoluteArtifactFilename=true|grep jar|cut -d ':' -f 5,6|sort|cut -d ':' -f 2)|xargs echo|tr ' ' ':')
scala $JAVA_OPTS -nobootcp -toolcp $CLASSPATH
