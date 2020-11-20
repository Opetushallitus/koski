#!/usr/bin/env bash
set -Euxo pipefail

wait-for-url() {
    echo "Testing $1";
    timeout --preserve-status --signal TERM 600 bash -c \
    'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 5;\
    done' ${1};
}

sudo systemctl stop koski && \
cd /home/ubuntu/koski && \
git fetch && \
git reset --hard HEAD && \
git clean -f -d && \
git pull && \
make build && \
docker-compose down && \
docker volume ls -q | xargs docker volume rm && \
docker-compose up -d && \
sleep 30 && \
sudo systemctl start koski && \
wait-for-url https://sandbox.dev.koski.opintopolku.fi/koski/api/healtcheck && \
PGPASSWORD=oph psql --host localhost --port 5432 --username oph --dbname koski --file sandbox/read_only_koski.sql

