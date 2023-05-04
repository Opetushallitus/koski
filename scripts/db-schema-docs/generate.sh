#!/bin/bash

readonly target_dir="../../documentation/tietokantaskeemat"
readonly schemaspy_url="https://github.com/schemaspy/schemaspy/releases/download/v6.2.2/schemaspy-6.2.2.jar"
readonly postgres_url="https://jdbc.postgresql.org/download/postgresql-42.6.0.jar"

if [ ! -f bin/schemaspy.jar ]; then
    echo "Download SchemaSpy..."
    curl -s -L -o bin/schemaspy.jar "${schemaspy_url}"
fi

if [ ! -f bin/postgresql.jar ]; then
    echo "Download PostgreSQL driver..."
    curl -s -L -o bin/postgresql.jar "${postgres_url}"
fi

rm -rf db-docs

echo "Generate KOSKI operative database documentation..."
java -jar bin/schemaspy.jar -t pgsql11 -dp bin/postgresql.jar -db koski -host localhost -port 5432 -s oph -u oph -p oph -o "$target_dir/koski" -vizjs

echo "Generate KOSKI raportointikanta database documentation..."
java -jar bin/schemaspy.jar -t pgsql11 -dp bin/postgresql.jar -db raportointikanta -host localhost -port 5432 -s public -u oph -p oph -o "$target_dir/koski-raportointikanta" -vizjs

echo "Generate Valpas database documentation..."
java -jar bin/schemaspy.jar -t pgsql11 -dp bin/postgresql.jar -db valpas -host localhost -port 5432 -s public -u oph -p oph -o "$target_dir/valpas" -vizjs
