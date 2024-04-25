#!/bin/bash

set -euo pipefail

fixt_context="$1"
fixt_date="${2:-"2021-09-05"}"

function dump {
    local -r dump_db="$1"
    local -r dump_file="$2"

    echo "Take snapshot of $dump_db to $dump_file..."
    PGPASSWORD=oph pg_dump -h localhost -p 5432 -U oph -b "$dump_db" > "$dump_file"
}

function make_snapshot {
    local -r snap_date="$1"
    local -r script_dir=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
    local -r snap_path=$(realpath "$script_dir/../src/main/resources/fixtures/$fixt_context/$snap_date")

    mkdir -p "$snap_path"
    dump "koski" "$snap_path/koski.sql"
    dump "raportointikanta" "$snap_path/raportointikanta.sql"
    dump "valpas" "$snap_path/valpas.sql"
}

# KOSKI
if [ "$fixt_context" == "koski" ]; then
    echo "Generate KOSKI fixture..."
    curl -u "pää:pää" -X POST "http://localhost:7021/koski/fixtures/reset?reloadRaportointikanta=1&reloadYTR=1"
    echo
    make_snapshot "default"
fi

# Valpas
if [ "$fixt_context" == "valpas" ]; then
    echo "Generate Valpas fixture..."
    curl -X GET "http://localhost:7021/koski/valpas/test/reset-mock-data/$fixt_date"
    echo
    make_snapshot "$fixt_date"
fi
