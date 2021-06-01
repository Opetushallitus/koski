FROM adoptopenjdk/openjdk11:alpine-slim

ARG KOSKI_VERSION
ARG PROMETHEUS_JMX_EXPORTER_VERSION="0.14.0"
ARG PROMETHEUS_JMX_EXPORTER_JAR_HASH="5ead661727d1e7ed4cf660c0904c71d93e01ebb8c744160bd122442580fe5206"

# Install:
# * tzdata for timezones
# * fonts (see: https://github.com/docker-library/openjdk/issues/73#issuecomment-207816707)
RUN apk add --no-cache tzdata ttf-dejavu

# Set timezone
RUN cp /usr/share/zoneinfo/Europe/Helsinki /etc/localtime && apk del tzdata

# JVM reads timezone from this file instead:
RUN echo 'Europe/Helsinki' > /etc/timezone

# Install Prometheus JMX exporter
RUN wget -q https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/${PROMETHEUS_JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${PROMETHEUS_JMX_EXPORTER_VERSION}.jar \
    -O /usr/local/bin/jmx_prometheus_javaagent.jar && \
    echo "$PROMETHEUS_JMX_EXPORTER_JAR_HASH  /usr/local/bin/jmx_prometheus_javaagent.jar" | sha256sum -c
COPY jmx_exporter_config.yml /etc
COPY run.sh /usr/local/bin
RUN chmod +x /usr/local/bin/run.sh
RUN addgroup -S koski -g 10001 && adduser -u 10000 -S -G koski koski

# Defang bins
RUN find / -xdev -perm +6000 -type f -exec chmod a-s {} \; || true

USER koski
RUN mkdir -p /home/koski/heapdumps
COPY target/dist/target/koski-${KOSKI_VERSION}.war /home/koski
RUN unzip -d /home/koski /home/koski/koski-${KOSKI_VERSION}.war && rm /home/koski/koski-${KOSKI_VERSION}.war
COPY log4j.properties /home/koski

# Koski app
EXPOSE 8080
# Prometheus JMX exporter
EXPOSE 9101
# JMX port
EXPOSE 5555

VOLUME /tmp

ENTRYPOINT /usr/local/bin/run.sh
