# Voit päivittää imagen komennolla ./update-openjdk.sh
FROM adoptopenjdk/openjdk11:jdk-11.0.26_4-alpine-slim@sha256:5ab2a129d1054f0300b4bfb4f6b298af1be4e746d9b9b0ac680ef21cfc652948

ARG KOSKI_VERSION
ARG PROMETHEUS_JMX_EXPORTER_VERSION="1.2.0"
ARG PROMETHEUS_JMX_EXPORTER_JAR_HASH="3a87f7f9df4ff79741d53ccd24aba75176481ececc80f3797dea8413fa3ed2ec"

# Install:
# * tzdata for timezones
# * fonts (see: https://github.com/docker-library/openjdk/issues/73#issuecomment-207816707)
RUN apk add --no-cache tzdata ttf-dejavu

# Set timezone
RUN cp /usr/share/zoneinfo/Europe/Helsinki /etc/localtime && apk del tzdata

# JVM reads timezone from this file instead:
RUN echo 'Europe/Helsinki' > /etc/timezone

# Install Prometheus JMX exporter
RUN wget -q https://github.com/prometheus/jmx_exporter/releases/download/${PROMETHEUS_JMX_EXPORTER_VERSION}/jmx_prometheus_javaagent-${PROMETHEUS_JMX_EXPORTER_VERSION}.jar \
    -O /usr/local/bin/jmx_prometheus_javaagent.jar && \
    echo "$PROMETHEUS_JMX_EXPORTER_JAR_HASH  /usr/local/bin/jmx_prometheus_javaagent.jar" | sha256sum -c
COPY docker-build/jmx_exporter_config.yml /etc

COPY docker-build/run.sh /usr/local/bin
RUN chmod +x /usr/local/bin/run.sh

RUN addgroup -S koski -g 10001 && adduser -u 10000 -S -G koski koski

# Defang bins
RUN find / -xdev -perm +6000 -type f -exec chmod a-s {} \; || true

USER koski

RUN mkdir -p /home/koski/heapdumps

COPY target/koski-${KOSKI_VERSION}.war /home/koski
RUN unzip -d /home/koski /home/koski/koski-${KOSKI_VERSION}.war && rm /home/koski/koski-${KOSKI_VERSION}.war

COPY docker-build/log4j2.xml /home/koski

# Koski app
EXPOSE 8080
# Prometheus JMX exporter
EXPOSE 9101
# JMX
EXPOSE 5555

VOLUME /tmp

ENTRYPOINT /usr/local/bin/run.sh
