#!/usr/bin/env bash
set -Euxo pipefail

wait-for-url() {
    echo "Testing $1";
    timeout --preserve-status --signal TERM 1200 bash -c \
    'while [[ "$(curl -s -o /dev/null -L -w ''%{http_code}'' ${0})" != "200" ]];\
    do echo "Waiting for ${0}" && sleep 30;\
    done' ${1};
}

export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" && \ # This loads nvm
nvm install 14 && \
sudo systemctl stop koski && \
rm -rf /home/ubuntu/koski && \
cd /home/ubuntu && \
git clone https://github.com/Opetushallitus/koski.git && \
cd /home/ubuntu/koski && \
make build && \
docker compose down && \
docker volume ls -q | xargs docker volume rm && \
docker compose up -d && \
rm -rf /home/ubuntu/koski/log && \
ln -s /home/ubuntu/log/ /home/ubuntu/koski/ && \
sleep 30 && \
sudo systemctl start koski && \
wait-for-url https://sandbox.dev.koski.opintopolku.fi/koski/api/healtcheck && \
PGPASSWORD=oph psql --host localhost --port 5432 --username oph --dbname koski --file sandbox/read_only_koski.sql
