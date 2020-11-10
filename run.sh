ROOT=/home/koski

JAVA_OPTS="\
-Xms1g -Xmx6g \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/home/koski/heapdumps/koski-heap.dump \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.port=5555 \
-Dcom.sun.management.jmxremote.rmi.port=5555 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=localhost \
-Dkoski.port=8080 \
-DHOSTNAME="$(hostname)" \
-Dlog4j.configuration=file:///home/koski/log4j.properties \
-Dlog4j.log.dir=/home/koski/logs \
-Dfile.encoding=UTF-8 \
-Dresourcebase=. \
"

cd $ROOT
JAR=$(ls WEB-INF/lib/koski*)
java $JAVA_OPTS -classpath "$JAR:WEB-INF/lib/*" "fi.oph.koski.jettylauncher.JettyLauncher"
