ROOT=/home/koski
cd $ROOT || exit 1

JAVA_OPTS="\
-Xms${JAVA_XMX:-1g} -Xmx${JAVA_XMX:-1g} \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/home/koski/heapdumps/koski-heap.dump \
-Dcom.sun.management.jmxremote.ssl=false \
-Dcom.sun.management.jmxremote.port=5555 \
-Dcom.sun.management.jmxremote.rmi.port=5555 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Djava.rmi.server.hostname=localhost \
-Djava.io.tmpdir=/tmp \
-Dkoski.port=8080 \
-DHOSTNAME="$(hostname)" \
-Dlog4j.configuration=file:///home/koski/log4j2.xml \
-Dlog4j.log.dir=/home/koski/logs \
-Dfile.encoding=UTF-8 \
-Dresourcebase=. \
-Dnetworkaddress.cache.ttl=30 \
"

JAR=$(ls WEB-INF/lib/koski*)

java $JAVA_OPTS \
  -javaagent:/usr/local/bin/jmx_prometheus_javaagent.jar=9101:/etc/jmx_exporter_config.yml \
  -classpath "$JAR:WEB-INF/lib/*" \
  "fi.oph.koski.jettylauncher.JettyLauncher"
