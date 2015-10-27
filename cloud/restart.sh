#!/bin/bash

# Environment variables DEPLOY_DIR and LOG_DIR are always set by the calling hook script.
# Also all the "run_environment" variables in the ansible group_vars are exported by the hook script.

export JAVA_OPTS="\
-Dtor.profile=cloud \
-Dtor.port=8080 \
-Dlog4j.configuration=file://$DEPLOY_DIR/src/main/resources/log4j.cloud.properties \
-Dlog4j.log.dir=$LOG_DIR \
-Dlog4j.redis.password=$LOG4_REDIS_PASSWORD \
-Dldap.host=ldap \
-Dldap.password=$LDAP_PASSWORD \
-Dldap.userdn=$LDAP_USERDN \
-Dopintopolku.virkailija.url=\"https://virkailija.tordev.tor.oph.reaktor.fi\" \
-Dauthentication-service.username=$AUTHENTICATION_SERVICE_USERNAME \
-Dauthentication-service.password=$AUTHENTICATION_SERVICE_PASSWORD \
-Deperusteet.url=\"https://eperusteet.opintopolku.fi/eperusteet-service\" \
-Dkoodisto.url=\"https://testi.virkailija.opintopolku.fi/koodisto-service\" \
"

pkill java
make build && { nohup make run &>> $LOG_DIR/tor.stdout.log & }
